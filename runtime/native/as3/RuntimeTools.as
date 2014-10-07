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
	}
}