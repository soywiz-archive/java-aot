package {
	public class Long {
		private var low:int;
		private var high:int;

		public function Long(low:int, high:int) {
			this.low = low;
			this.high = high;
		}

		static public function value(low:int, high:int):Long { return new Long(low, high); }

		static public function neg(value:Long):Long { throw(new Error("Not implemented")); }

		static public function cmp(l:Long, r:Long):int { throw(new Error("Not implemented")); }

		static public function add(l:Long, r:Long):Long { throw(new Error("Not implemented")); }
		static public function sub(l:Long, r:Long):Long { throw(new Error("Not implemented")); }

		static public function mul(l:Long, r:Long):Long { throw(new Error("Not implemented")); }
		static public function div(l:Long, r:Long):Long { throw(new Error("Not implemented")); }
		static public function mod(l:Long, r:Long):Long { throw(new Error("Not implemented")); }

		static public function and(l:Long, r:Long):Long { return new Long(l.low & r.low, l.high & r.high); }
		static public function or(l:Long, r:Long):Long { return new Long(l.low | r.low, l.high | r.high); }
		static public function xor(l:Long, r:Long):Long { return new Long(l.low ^ r.low, l.high ^ r.high); }

		static public function shl(l:Long, r:Long):Long { throw(new Error("Not implemented")); }
		static public function shr(l:Long, r:Long):Long { throw(new Error("Not implemented")); }
		static public function sar(l:Long, r:Long):Long { throw(new Error("Not implemented")); }

		static public function eq(l:Long, r:Long):Boolean { throw(new Error("Not implemented")); }
		static public function ne(l:Long, r:Long):Boolean { throw(new Error("Not implemented")); }
		static public function ge(l:Long, r:Long):Boolean { throw(new Error("Not implemented")); }
		static public function le(l:Long, r:Long):Boolean { throw(new Error("Not implemented")); }
		static public function gt(l:Long, r:Long):Boolean { throw(new Error("Not implemented")); }
		static public function lt(l:Long, r:Long):Boolean { throw(new Error("Not implemented")); }
	}
}
