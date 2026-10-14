with Ada.Streams;

package Class_File_Reader is
   Class_File_Error : exception;

   Max_Method_Code_Length : constant := 65_535;
   subtype Method_Code_Buffer is Ada.Streams.Stream_Element_Array
     (1 .. Max_Method_Code_Length);

   procedure Extract_Static_Method
     (Class_Bytes : Ada.Streams.Stream_Element_Array;
      Method_Name : String;
      Descriptor  : String;
      Code        : out Method_Code_Buffer;
      Code_Length : out Natural);
end Class_File_Reader;
