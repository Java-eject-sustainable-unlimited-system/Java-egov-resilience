with Ada.Command_Line;
with Ada.Exceptions;
with Ada.Streams;
with Ada.Streams.Stream_IO;
with Class_File_Reader;
with Ada.Text_IO;
with Bytecode_VM;
with Interfaces;

procedure Ada_Jvm is
   use Ada.Command_Line;
   use Ada.Streams;
   use Ada.Text_IO;
   use type Ada.Streams.Stream_IO.Count;
   use type Interfaces.Integer_32;

   Max_Code_Length : constant Stream_Element_Offset := 65_536;
   subtype Code_Buffer is Stream_Element_Array (1 .. Max_Code_Length);
   Max_Class_File_Length : constant Stream_IO.Count := 16 * 1024 * 1024;

   type Stream_Element_Array_Access is access Stream_Element_Array;

   procedure Check
     (Code     : Stream_Element_Array;
      Expected : Interfaces.Integer_32)
   is
      Actual : constant Interfaces.Integer_32 := Bytecode_VM.Execute (Code);
   begin
      if Actual /= Expected then
         raise Program_Error with
           "self-test failed: expected"
           & Interfaces.Integer_32'Image (Expected)
           & ", received" & Interfaces.Integer_32'Image (Actual);
      end if;
   end Check;

   procedure Run_Self_Tests is
   begin
      Check ([16#10#, 8, 16#10#, 5, 16#60#, 16#AC#], 13);
      Check ([16#02#, 16#10#, 3, 16#6C#, 16#AC#], 0);
      Check ([16#10#, 16#FD#, 16#AC#], -3);
      Check ([16#10#, 7, 16#10#, 6, 16#68#, 16#AC#], 42);
      Check ([16#10#, 9, 16#10#, 4, 16#64#, 16#AC#], 5);
      Put_Line ("Ada JVM bytecode self-tests passed.");
   end Run_Self_Tests;

   procedure Run_File (Path : String) is
      File   : Ada.Streams.Stream_IO.File_Type;
      Buffer : Code_Buffer;
      Last   : Stream_Element_Offset;
   begin
      Ada.Streams.Stream_IO.Open
        (File, Ada.Streams.Stream_IO.In_File, Path);
      if Ada.Streams.Stream_IO.Size (File) >
        Ada.Streams.Stream_IO.Count (Max_Code_Length)
      then
         raise Bytecode_VM.Bytecode_Error with
           "bytecode input exceeds 64 KiB";
      end if;
      Ada.Streams.Stream_IO.Read (File, Buffer, Last);
      Ada.Streams.Stream_IO.Close (File);

      if Last = 0 then
         raise Bytecode_VM.Bytecode_Error with "empty bytecode input";
      end if;
      Put_Line
        (Interfaces.Integer_32'Image
           (Bytecode_VM.Execute (Buffer (1 .. Last))));
   exception
      when others =>
         if Ada.Streams.Stream_IO.Is_Open (File) then
            Ada.Streams.Stream_IO.Close (File);
         end if;
         raise;
   end Run_File;

   procedure Run_Class_File
     (Path       : String;
      Method     : String;
      Descriptor : String)
   is
      File        : Ada.Streams.Stream_IO.File_Type;
      Size        : Stream_IO.Count;
      Buffer      : Stream_Element_Array_Access := null;
      Last        : Stream_Element_Offset;
      Code        : Class_File_Reader.Method_Code_Buffer;
      Code_Length : Natural;
   begin
      Stream_IO.Open (File, Stream_IO.In_File, Path);
      Size := Stream_IO.Size (File);
      if Size = 0 or else Size > Max_Class_File_Length then
         raise Class_File_Reader.Class_File_Error with
           "class-file input must be between 1 byte and 16 MiB";
      end if;
      Buffer :=
        new Stream_Element_Array
          (1 .. Stream_Element_Offset (Size));
      Stream_IO.Read (File, Buffer.all, Last);
      Stream_IO.Close (File);

      Class_File_Reader.Extract_Static_Method
        (Class_Bytes => Buffer (1 .. Last),
         Method_Name => Method,
         Descriptor  => Descriptor,
         Code        => Code,
         Code_Length => Code_Length);
      Put_Line
        (Interfaces.Integer_32'Image
           (Bytecode_VM.Execute
              (Code (1 .. Stream_Element_Offset (Code_Length)))));
   exception
      when others =>
         if Stream_IO.Is_Open (File) then
            Stream_IO.Close (File);
         end if;
         raise;
   end Run_Class_File;
begin
   if Argument_Count = 1 and then Argument (1) = "--self-test" then
      Run_Self_Tests;
   elsif Argument_Count = 1 then
      Run_File (Argument (1));
   elsif Argument_Count = 4 and then Argument (1) = "--class-file" then
      Run_Class_File (Argument (2), Argument (3), Argument (4));
   else
      Put_Line
        (Standard_Error,
         "Usage: ada_jvm --self-test"
         & " | ada_jvm <raw-method-code-file>"
         & " | ada_jvm --class-file <file.class> <method> <descriptor>");
      Set_Exit_Status (Failure);
   end if;
exception
   when Error : Bytecode_VM.Bytecode_Error =>
   Put_Line
     (Standard_Error,
      "Bytecode error: " & Ada.Exceptions.Exception_Message (Error));
   Set_Exit_Status (Failure);
   when Error : Class_File_Reader.Class_File_Error =>
     Put_Line
       (Standard_Error,
        "Class-file error: " & Ada.Exceptions.Exception_Message (Error));
     Set_Exit_Status (Failure);
   when Error : others =>
   Put_Line
     (Standard_Error,
      Ada.Exceptions.Exception_Information (Error));
      Set_Exit_Status (Failure);
end Ada_Jvm;
