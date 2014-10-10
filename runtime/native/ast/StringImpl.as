package {
	public class StringImpl {
		[Inline] static public function toUpperCase(that:String):String { return that.toUpperCase(); }
		[Inline] static public function length(that:String):int { return that.length; }
		[Inline] static public function charAt_int(that:String, pos:int):int { return that.charCodeAt(pos); }
		[Inline] static public function __init___char$Array(chars:Array):String {
			return __init___char$Array_int_int(chars, 0, chars.length);
		}
		static public function __init___char$Array_int_int(chars:Array, offset:int, length:int):String {
			var out:String = "";
			for (var n:int = 0; n < length; n++) out += String.fromCharCode(chars[offset + n]);
			return out;
		}
	}
}