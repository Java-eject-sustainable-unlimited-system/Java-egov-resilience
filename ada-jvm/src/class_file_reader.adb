with Ada.Strings.Unbounded;
with Ada.Unchecked_Deallocation;
with Interfaces;

package body Class_File_Reader is
   use type Ada.Streams.Stream_Element;
   use type Ada.Streams.Stream_Element_Offset;
   use type Interfaces.Unsigned_32;

   type Constant_Pool_Entry is record
      Tag  : Ada.Streams.Stream_Element := 0;
      Text : Ada.Strings.Unbounded.Unbounded_String;
   end record;

   type Constant_Pool is array (Natural range <>) of Constant_Pool_Entry;
   type Constant_Pool_Access is access Constant_Pool;
   procedure Release is new Ada.Unchecked_Deallocation
     (Constant_Pool, Constant_Pool_Access);

   procedure Extract_Static_Method
     (Class_Bytes : Ada.Streams.Stream_Element_Array;
      Method_Name : String;
      Descriptor  : String;
      Code        : out Method_Code_Buffer;
      Code_Length : out Natural)
   is
      Position : Natural := 0;
      Pool     : Constant_Pool_Access := null;
      Found    : Boolean := False;

      function Remaining return Natural is
        (Class_Bytes'Length - Position);

      procedure Require (Count : Natural) is
      begin
         if Count > Remaining then
            raise Class_File_Error with "truncated class file";
         end if;
      end Require;

      function Read_U1 return Ada.Streams.Stream_Element is
      begin
         Require (1);
         Position := Position + 1;
         return Class_Bytes
           (Class_Bytes'First + Ada.Streams.Stream_Element_Offset (Position - 1));
      end Read_U1;

      function Read_U2 return Natural is
         High : constant Natural := Natural (Read_U1);
         Low  : constant Natural := Natural (Read_U1);
      begin
         return High * 256 + Low;
      end Read_U2;

      function Read_U4 return Interfaces.Unsigned_32 is
         Value : Interfaces.Unsigned_32 := 0;
      begin
         for Index in 1 .. 4 loop
            Value := Value * 256 + Interfaces.Unsigned_32 (Read_U1);
         end loop;
         return Value;
      end Read_U4;

      procedure Skip (Count : Natural) is
      begin
         Require (Count);
         Position := Position + Count;
      end Skip;

      function Utf8 (Index : Natural) return String is
      begin
         if Pool = null
           or else Index = 0
           or else Index > Pool'Last
           or else Pool (Index).Tag /= 1
         then
            raise Class_File_Error with "invalid constant-pool UTF-8 index";
         end if;
         return Ada.Strings.Unbounded.To_String (Pool (Index).Text);
      end Utf8;

      procedure Skip_Attributes (Count : Natural) is
      begin
         for Index in 1 .. Count loop
            declare
               Name_Index : constant Natural := Read_U2;
               Length     : constant Interfaces.Unsigned_32 := Read_U4;
            begin
               if Length > Interfaces.Unsigned_32 (Remaining) then
                  raise Class_File_Error with "truncated class-file attribute";
               end if;
               declare
                  Ignore_Name : constant String := Utf8 (Name_Index);
               begin
                  null;
               end;
               Skip (Natural (Length));
            end;
         end loop;
      end Skip_Attributes;

      procedure Read_Code_Attribute
        (Attribute_Length : Interfaces.Unsigned_32)
      is
         Start       : constant Natural := Position;
         Length      : Interfaces.Unsigned_32;
         Exceptions  : Natural;
         Attributes  : Natural;
      begin
         Skip (4); -- max_stack and max_locals
         Length := Read_U4;
         if Length = 0 then
            raise Class_File_Error with "method bytecode must not be empty";
         elsif Length > Interfaces.Unsigned_32 (Max_Method_Code_Length) then
            raise Class_File_Error with "method bytecode exceeds 65535 bytes";
         elsif Length > Interfaces.Unsigned_32 (Remaining) then
            raise Class_File_Error with "truncated method bytecode";
         end if;

         Code_Length := Natural (Length);
         for Index in 1 .. Code_Length loop
            Code (Ada.Streams.Stream_Element_Offset (Index)) := Read_U1;
         end loop;

         Exceptions := Read_U2;
         if Exceptions > Remaining / 8 then
            raise Class_File_Error with "truncated exception table";
         end if;
         Skip (Exceptions * 8);
         Attributes := Read_U2;
         Skip_Attributes (Attributes);

         if Position - Start /= Natural (Attribute_Length) then
            raise Class_File_Error with "invalid Code attribute length";
         end if;
      end Read_Code_Attribute;

      procedure Read_Members (Is_Method : Boolean) is
         Count : constant Natural := Read_U2;
      begin
         for Member in 1 .. Count loop
            declare
               Access_Flags : constant Natural := Read_U2;
               Name_Index   : constant Natural := Read_U2;
               Type_Index   : constant Natural := Read_U2;
               Attributes   : constant Natural := Read_U2;
               Member_Name  : constant String := Utf8 (Name_Index);
               Member_Type  : constant String := Utf8 (Type_Index);
               Is_Target    : Boolean := False;
            begin
               if Is_Method then
                  Is_Target :=
                    Member_Name = Method_Name
                    and then Member_Type = Descriptor;
               end if;

               for Attribute in 1 .. Attributes loop
                  declare
                     Attribute_Name_Index : constant Natural := Read_U2;
                     Attribute_Length     : constant Interfaces.Unsigned_32 :=
                       Read_U4;
                     Attribute_Name       : constant String :=
                       Utf8 (Attribute_Name_Index);
                  begin
                     if Attribute_Length > Interfaces.Unsigned_32 (Remaining) then
                        raise Class_File_Error with
                          "truncated class-file attribute";
                     end if;

                     if Is_Target and then Attribute_Name = "Code" then
                        if Found then
                           raise Class_File_Error with
                             "duplicate matching method";
                        elsif (Access_Flags / 8) mod 2 = 0 then
                           raise Class_File_Error with
                             "selected method is not static";
                        end if;
                        Read_Code_Attribute (Attribute_Length);
                        Found := True;
                     else
                        Skip (Natural (Attribute_Length));
                     end if;
                  end;
               end loop;
            end;
         end loop;
      end Read_Members;

      Pool_Count : Natural;
      Major      : Natural;
   begin
      Code_Length := 0;
      if Class_Bytes'Length < 10 then
         raise Class_File_Error with "class file is too short";
      end if;
      if Read_U4 /= 16#CAFEBABE# then
         raise Class_File_Error with "invalid class-file magic";
      end if;

      Skip (2); -- minor version
      Major := Read_U2;
      if Major < 45 or else Major > 69 then
         raise Class_File_Error with "unsupported class-file version";
      end if;

      Pool_Count := Read_U2;
      if Pool_Count = 0 then
         raise Class_File_Error with "invalid constant-pool count";
      end if;
      Pool := new Constant_Pool (1 .. Pool_Count - 1);

      declare
         Index : Natural := 1;
      begin
         while Index < Pool_Count loop
            declare
               Tag : constant Ada.Streams.Stream_Element := Read_U1;
            begin
               Pool (Index).Tag := Tag;
               case Tag is
                  when 1 =>
                     declare
                        Length : constant Natural := Read_U2;
                        Text   : Ada.Strings.Unbounded.Unbounded_String;
                     begin
                        Require (Length);
                        for Byte in 1 .. Length loop
                           Ada.Strings.Unbounded.Append
                             (Text, Character'Val (Natural (Read_U1)));
                        end loop;
                        Pool (Index).Text := Text;
                     end;
                  when 3 | 4 =>
                     Skip (4);
                  when 5 | 6 =>
                     if Index + 1 >= Pool_Count then
                        raise Class_File_Error with
                          "invalid two-slot constant-pool entry";
                     end if;
                     Skip (8);
                     Index := Index + 1;
                  when 7 | 8 | 16 | 19 | 20 =>
                     Skip (2);
                  when 9 | 10 | 11 | 12 | 17 | 18 =>
                     Skip (4);
                  when 15 =>
                     Skip (3);
                  when others =>
                     raise Class_File_Error with
                       "unsupported constant-pool tag"
                       & Ada.Streams.Stream_Element'Image (Tag);
               end case;
            end;
            Index := Index + 1;
         end loop;
      end;

      Skip (6); -- access flags, this_class, and super_class
      declare
         Interfaces : constant Natural := Read_U2;
      begin
         Skip (Interfaces * 2);
      end;
      Read_Members (Is_Method => False);
      Read_Members (Is_Method => True);
      Skip_Attributes (Read_U2);

      if Position /= Class_Bytes'Length then
         raise Class_File_Error with "trailing data after class file";
      elsif not Found then
         raise Class_File_Error with "selected static method was not found";
      end if;
      Release (Pool);
   exception
      when others =>
         if Pool /= null then
            Release (Pool);
         end if;
         raise;
   end Extract_Static_Method;
end Class_File_Reader;
