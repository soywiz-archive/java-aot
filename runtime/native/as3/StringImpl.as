package {
	public class StringImpl {
		[Inline] static public function toUpperCase_(that:String):String { return that.toUpperCase(); }
		[Inline] static public function length_(that:String):int { return that.length; }
		[Inline] static public function charAt_int(that:String, pos:int):int { return that.charCodeAt(pos); }
		static public function __init___char$Array(chars:Array):String { return String.fromCharCode.apply(null, chars); }
		static public function __init___char$Array_int_int(chars:Array, offset:int, length:int):String {
			return String.fromCharCode.apply(null, chars.slice(offset, offset + length));
		}
	}
}