package libcore {
	public class Native {
		static public function lower(v:int):int { return String.fromCharCode(v).toLowerCase().charCodeAt(0); }
		static public function upper(v:int):int { return String.fromCharCode(v).toUpperCase().charCodeAt(0); }
		static public function atan(v:Number):Number { return Math.atan(v); }
		static public function cos(v:Number):Number { return Math.cos(v); }
		static public function sin(v:Number):Number { return Math.sin(v); }
		static public function intBitsToFloat(v:int):Number { throw(new Error("Not implemented")); }
		static public function isNaN(v:Number):Boolean { return isNaN2(v); }
		static public function putchar(v:int):void {}
		static public function flush():void { }
		static public function debugint(v:int):void { trace('v:' + v); }
		static public function exit(status:int):void { throw(new Error("Not implemented")); }
		static public function arraycopy(src:*, srcOfs:int, dest:*, destOfs:int, len:int):void { throw(new Error("Not implemented")); }
		static public function gc():void { System.gc(); }
		static public function currentTimeMillis():Long { return new Long(0, 0); }
	}
}

function isNaN2(value:Number):Boolean { return isNaN(value); }