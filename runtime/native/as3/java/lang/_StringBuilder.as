package java.lang {
	public class _StringBuilder {
		private var result:String = "";

		public function __init___():void {
		}

		public function append_java_lang_String(value:String):_StringBuilder {
			this.result += value;
			return this;
		}

		public function append_java_lang_Object(value:Object):java.lang._StringBuilder { return append_java_lang_String(String(value)); }
		public function append_float(value:Number):java.lang._StringBuilder { return this.append_java_lang_String(String(value)); }
		public function append_double(value:Number):_StringBuilder { return this.append_java_lang_String(String(value)); }
		public function append_long(value:Long):_StringBuilder { return this.append_java_lang_String(value.toString()); }
		public function append_int(value:int):_StringBuilder { return this.append_java_lang_String(String(value)); }
		public function toString_():String { return this.result; }
	}
}