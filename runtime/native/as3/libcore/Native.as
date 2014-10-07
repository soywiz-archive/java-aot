package libcore {
	import flash.text.TextField;
	import flash.utils.ByteArray;

	public class Native {
		static public var consoleTextField:TextField;

		static public function lower_char(v:int):int { return String.fromCharCode(v).toLowerCase().charCodeAt(0); }
		static public function upper_char(v:int):int { return String.fromCharCode(v).toUpperCase().charCodeAt(0); }
		static public function atan_double(v:Number):Number { return Math.atan(v); }
		static public function cos_double(v:Number):Number { return Math.cos(v); }
		static public function sin_double(v:Number):Number { return Math.sin(v); }

		static private var convBA:ByteArray = new ByteArray();
		static public function intBitsToFloat_int(v:int):Number {
			convBA.position = 0;
			convBA.writeInt(v);
			convBA.position = 0;
			return convBA.readFloat();
		}
		static public function isNaN_float(v:Number):Boolean { return isNaN(v); }
		static public function putchar_char(v:int):void { Native.consoleTextField.text += String.fromCharCode(v); }
		static public function flush():void { }
		static public function debugint_int(v:int):void { trace('v:' + v); }
		static public function exit_int(status:int):void { throw(new Error("Not implemented")); }
		static public function arraycopy_java_lang_Object_int_java_lang_Object_int_int(src:*, srcOfs:int, dest:*, destOfs:int, len:int):void {
			var srcArray:Array = (src as Array);
			var destArray:Array = (dest as Array);
			for (var n:int = 0; n < len; n++) destArray[n + destOfs] = srcArray[n + srcOfs];
		}
		static public function gc():void { }
		static public function currentTimeMillis():Long { return Long.fromNumber(new Date().getTime()); }
	}
}
