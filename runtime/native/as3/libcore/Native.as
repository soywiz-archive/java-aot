package libcore {
	import flash.text.TextField;

	public class Native {
		static public var consoleTextField:TextField;

		static public function lower_char(v:int):int { return String.fromCharCode(v).toLowerCase().charCodeAt(0); }
		static public function upper_char(v:int):int { return String.fromCharCode(v).toUpperCase().charCodeAt(0); }
		static public function atan_double(v:Number):Number { return Math.atan(v); }
		static public function cos_double(v:Number):Number { return Math.cos(v); }
		static public function sin_double(v:Number):Number { return Math.sin(v); }
		static public function intBitsToFloat_int(v:int):Number { throw(new Error("Not implemented")); }
		static public function isNaN_float(v:Number):Boolean { return isNaN(v); }
		static public function putchar_char(v:int):void { Native.consoleTextField.text += String.fromCharCode(v); }
		static public function flush_():void { }
		static public function debugint_int(v:int):void { trace('v:' + v); }
		static public function exit_int(status:int):void { throw(new Error("Not implemented")); }
		static public function arraycopy_java_lang_Object_int_java_lang_Object_int_int(src:*, srcOfs:int, dest:*, destOfs:int, len:int):void { throw(new Error("Not implemented")); }
		static public function gc_():void { }
		static public function currentTimeMillis_():Long { return new Long(0, 0); }
	}
}
