package {
	public class RuntimeTools {
		static public function toByte(value:int):int {
			var result:int = (value & 0xFF);
			if (value < 0) result |= ~0xFF;
			return result;
		}

		static public function toShort(value:int):int {
			var result:int = (value & 0xFFFF);
			if (value < 0) result |= ~0xFFFF;
			return result;
		}

		static public function number_cmp(l:Number, r:Number):int {
			if (l < r) return -1;
			if (l > r) return +1;
			return 0;
		}

		static public function number_cmpl(l:Number, r:Number):int { return (isNaN(l) || isNaN(r)) ? -1 : number_cmp(l, r); }
		static public function number_cmpg(l:Number, r:Number):int { return (isNaN(l) || isNaN(r)) ? +1 : number_cmp(l, r); }
	}
}