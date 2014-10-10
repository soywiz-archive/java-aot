package java.lang {
	public class StringBuilder {
		private var result:String = "";

		public function __init__():void {
		}

		public function append_java_lang_String(value:String):StringBuilder {
			this.result += value;
			return this;
		}

		public function append_java_lang_Object(value:Object):java.lang.StringBuilder { return append_java_lang_String(String(value)); }
		public function append_float(value:Number):java.lang.StringBuilder { return this.append_java_lang_String(String(value)); }
		public function append_double(value:Number):StringBuilder { return this.append_java_lang_String(String(value)); }
		public function append_long(value:Long):StringBuilder { return this.append_java_lang_String(value.toString()); }
		public function append_int(value:int):StringBuilder { return this.append_java_lang_String(String(value)); }
		public function toString():String { return this.result; }
	}
}