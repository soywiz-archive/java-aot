package {
	// http://docs.closure-library.googlecode.com/git/local_closure_goog_math_long.js.source.html
	public class Long {
		static private var TWO_PWR_16_DBL_:Number = 1 << 16;
		static private var TWO_PWR_24_DBL_:Number = 1 << 24;
		static private var TWO_PWR_32_DBL_:Number = TWO_PWR_16_DBL_ * TWO_PWR_16_DBL_;
		static private var TWO_PWR_31_DBL_:Number = TWO_PWR_32_DBL_ / 2;
		static private var TWO_PWR_48_DBL_:Number = TWO_PWR_32_DBL_ * TWO_PWR_16_DBL_;
		static private var TWO_PWR_64_DBL_:Number = TWO_PWR_32_DBL_ * TWO_PWR_32_DBL_;
		static private var TWO_PWR_63_DBL_:Number = TWO_PWR_64_DBL_ / 2;
		static private var ZERO:Long = Long.fromInt(0);
		static private var ONE:Long = Long.fromInt(1);
		static private var NEG_ONE:Long = Long.fromInt(-1);
		static private var MAX_VALUE:Long = Long.fromBits(0xFFFFFFFF | 0, 0x7FFFFFFF | 0);
		static private var MIN_VALUE:Long = Long.fromBits(0, 0x80000000 | 0);
		static private var TWO_PWR_24_:Long = Long.fromInt(1 << 24);


		private var low_:int;
		private var high_:int;

		public function Long(low:int, high:int) {
			this.low_ = low;
			this.high_ = high;
		}

		static public function fromInt(value:int):Long {
			return new Long(value | 0, value < 0 ? -1 : 0);
		}

		static public function fromString(str:String, radix:int = 10):Long {
			if (str.length == 0) throw Error('number format error: empty string');
			if (radix < 2 || 36 < radix) throw Error('radix out of range: ' + radix);

			if (str.charAt(0) == '-') {
				return Long.fromString(str.substring(1), radix).negate();
			} else if (str.indexOf('-') >= 0) {
				throw Error('number format error: interior "-" character: ' + str);
			}

			// Do several (8) digits each time through the loop, so as to
			// minimize the calls to the very expensive emulated div.
			var radixToPower:Long = Long.fromNumber(Math.pow(radix, 8));

			var result:Long = Long.ZERO;
			for (var i:int = 0; i < str.length; i += 8) {
				var size:int = Math.min(8, str.length - i);
				var value:int = parseInt(str.substring(i, i + size), radix);
				if (size < 8) {
					var power:Long = Long.fromNumber(Math.pow(radix, size));
					result = result.multiply(power).add(Long.fromNumber(value));
				} else {
					result = result.multiply(radixToPower);
					result = result.add(Long.fromNumber(value));
				}
			}
			return result;
		}

		static public function value(low:int, high:int):Long { return new Long(low, high); }
		static public function fromBits(low:int, high:int):Long { return new Long(low, high); }
		public static function fromNumber(value:Number):Long {
			if (isNaN(value) || !isFinite(value)) return Long.ZERO;
			if (value <= -Long.TWO_PWR_63_DBL_) return Long.MIN_VALUE;
			if (value + 1 >= Long.TWO_PWR_63_DBL_) return Long.MAX_VALUE;

			if (value < 0) {
				return Long.fromNumber(-value).negate();
			} else {
				return new Long((value % Long.TWO_PWR_32_DBL_) | 0, (value / Long.TWO_PWR_32_DBL_) | 0);
			}
		}

		public function getHighBits():int { return this.high_; }
		public function getLowBits():int { return this.low_; }
		public function getLowBitsUnsigned():Number { return (this.low_ >= 0) ? this.low_ : TWO_PWR_32_DBL_ + this.low_; }

		public function toInt():int { return low_; }
		public function toNumber():Number {
			return this.high_ * Long.TWO_PWR_32_DBL_ + this.getLowBitsUnsigned();
		}

		public function isZero():Boolean { return this.high_ == 0 && this.low_ == 0; }
		public function isNegative():Boolean { return this.high_ < 0; }
		public function isOdd():Boolean { return (this.low_ & 1) == 1; }

		public function negate():Long {
			if (this.equals(Long.MIN_VALUE)) return Long.MIN_VALUE;
			return this.not().add(Long.ONE);
		}

		public function compare(other:Long):int {
			if (this.equals(other)) return 0;

			var thisNeg:Boolean = this.isNegative();
			var otherNeg:Boolean = other.isNegative();
			if (thisNeg && !otherNeg) return -1;
			if (!thisNeg && otherNeg) return 1;

			// at this point, the signs are the same, so subtraction will not overflow
			return this.subtract(other).isNegative() ? -1 : 1;
		}

		public function add(other:Long):Long {
			// Divide each number into 4 chunks of 16 bits, and then sum the chunks.

			var a48:Number = this.high_ >>> 16;
			var a32:Number = this.high_ & 0xFFFF;
			var a16:Number = this.low_ >>> 16;
			var a00:Number = this.low_ & 0xFFFF;

			var b48:Number = other.high_ >>> 16;
			var b32:Number = other.high_ & 0xFFFF;
			var b16:Number = other.low_ >>> 16;
			var b00:Number = other.low_ & 0xFFFF;

			var c48:Number = 0, c32:Number = 0, c16:Number = 0, c00:Number = 0;
			c00 += a00 + b00;
			c16 += c00 >>> 16;
			c00 &= 0xFFFF;
			c16 += a16 + b16;
			c32 += c16 >>> 16;
			c16 &= 0xFFFF;
			c32 += a32 + b32;
			c48 += c32 >>> 16;
			c32 &= 0xFFFF;
			c48 += a48 + b48;
			c48 &= 0xFFFF;
			return Long.fromBits((c16 << 16) | c00, (c48 << 16) | c32);
		}

		public function subtract(other:Long):Long { return this.add(other.negate()); }

		public function multiply(other:Long):Long {
			if (this.isZero()) return Long.ZERO;
			if (other.isZero()) return Long.ZERO;

			if (this.equals(Long.MIN_VALUE)) {
				return other.isOdd() ? Long.MIN_VALUE : Long.ZERO;
			} else if (other.equals(Long.MIN_VALUE)) {
				return this.isOdd() ? Long.MIN_VALUE : Long.ZERO;
			}

			if (this.isNegative()) {
				if (other.isNegative()) {
					return this.negate().multiply(other.negate());
				} else {
					return this.negate().multiply(other).negate();
				}
			} else if (other.isNegative()) {
				return this.multiply(other.negate()).negate();
			}

			// If both longs are small, use float multiplication
			if (this.lessThan(Long.TWO_PWR_24_) &&
				other.lessThan(Long.TWO_PWR_24_)) {
				return Long.fromNumber(this.toNumber() * other.toNumber());
			}

			// Divide each long into 4 chunks of 16 bits, and then add up 4x4 products.
			// We can skip products that would overflow.

			var a48:Number = this.high_ >>> 16;
			var a32:Number = this.high_ & 0xFFFF;
			var a16:Number = this.low_ >>> 16;
			var a00:Number = this.low_ & 0xFFFF;

			var b48:Number = other.high_ >>> 16;
			var b32:Number = other.high_ & 0xFFFF;
			var b16:Number = other.low_ >>> 16;
			var b00:Number = other.low_ & 0xFFFF;

			var c48:Number = 0, c32:Number = 0, c16:Number = 0, c00:Number = 0;
			c00 += a00 * b00;
			c16 += c00 >>> 16;
			c00 &= 0xFFFF;
			c16 += a16 * b00;
			c32 += c16 >>> 16;
			c16 &= 0xFFFF;
			c16 += a00 * b16;
			c32 += c16 >>> 16;
			c16 &= 0xFFFF;
			c32 += a32 * b00;
			c48 += c32 >>> 16;
			c32 &= 0xFFFF;
			c32 += a16 * b16;
			c48 += c32 >>> 16;
			c32 &= 0xFFFF;
			c32 += a00 * b32;
			c48 += c32 >>> 16;
			c32 &= 0xFFFF;
			c48 += a48 * b00 + a32 * b16 + a16 * b32 + a00 * b48;
			c48 &= 0xFFFF;
			return Long.fromBits((c16 << 16) | c00, (c48 << 16) | c32);
		}

		public function div(other:Long):Long {
			if (other.isZero()) throw Error('division by zero');
			if (this.isZero()) return Long.ZERO;

			var rem:Long;

			if (this.equals(Long.MIN_VALUE)) {
				if (other.equals(Long.ONE) ||
					other.equals(Long.NEG_ONE)) {
					return Long.MIN_VALUE;  // recall that -MIN_VALUE == MIN_VALUE
				} else if (other.equals(Long.MIN_VALUE)) {
					return Long.ONE;
				} else {
					// At this point, we have |other| >= 2, so |this/other| < |MIN_VALUE|.
					var halfThis:Long = this.shiftRight(1);
					var approx:Long = halfThis.div(other).shiftLeft(1);
					if (approx.equals(Long.ZERO)) {
						return other.isNegative() ? Long.ONE : Long.NEG_ONE;
					} else {
						rem = this.subtract(other.multiply(approx));
						var result:Long = approx.add(rem.div(other));
						return result;
					}
				}
			} else if (other.equals(Long.MIN_VALUE)) {
				return Long.ZERO;
			}

			if (this.isNegative()) {
				if (other.isNegative()) {
					return this.negate().div(other.negate());
				} else {
					return this.negate().div(other).negate();
				}
			} else if (other.isNegative()) {
				return this.div(other.negate()).negate();
			}

			// Repeat the following until the remainder is less than other:  find a
			// floating-point that approximates remainder / other *from below*, add this
			// into the result, and subtract it from the remainder.  It is critical that
			// the approximate value is less than or equal to the real value so that the
			// remainder never becomes negative.
			var res:Long = Long.ZERO;
			rem = this;
			while (rem.greaterThanOrEqual(other)) {
				// Approximate the result of division. This may be a little greater or
				// smaller than the actual value.
				var approx2:Number = Math.max(1, Math.floor(rem.toNumber() / other.toNumber()));

				// We will tweak the approximate result by changing it in the 48-th digit or
				// the smallest non-fractional digit, whichever is larger.
				var log2:Number = Math.ceil(Math.log(approx2) / Math.LN2);
				var delta:Number = (log2 <= 48) ? 1 : Math.pow(2, log2 - 48);

				// Decrease the approximation until it is smaller than the remainder.  Note
				// that if it is too large, the product overflows and is negative.
				var approxRes:Long = Long.fromNumber(approx2);
				var approxRem:Long = approxRes.multiply(other);
				while (approxRem.isNegative() || approxRem.greaterThan(rem)) {
					approx2 -= delta;
					approxRes = Long.fromNumber(approx2);
					approxRem = approxRes.multiply(other);
				}

				// We know the answer can't be zero... and actually, zero would cause
				// infinite recursion since we would make no progress.
				if (approxRes.isZero()) {
					approxRes = Long.ONE;
				}

				res = res.add(approxRes);
				rem = rem.subtract(approxRem);
			}
			return res;
		}

		public function toString(radix:int = 10):String {
			if (radix < 2 || 36 < radix) throw Error('radix out of range: ' + radix);

			if (this.isZero()) return '0';

			var rem:Long;

			if (this.isNegative()) {
				if (this.equals(Long.MIN_VALUE)) {
					// We need to change the Long value before it can be negated, so we remove
					// the bottom-most digit in this base and then recurse to do the rest.
					var radixLong:Long = Long.fromNumber(radix);
					var div:Long = this.div(radixLong);
					rem = div.multiply(radixLong).subtract(this);

					return div.toString(radix) + Number(rem.toInt()).toString(radix);
				} else {
					return '-' + this.negate().toString(radix);
				}
			}

			// Do several (6) digits each time through the loop, so as to
			// minimize the calls to the very expensive emulated div.
			var radixToPower:Long = Long.fromNumber(Math.pow(radix, 6));

			rem = this;
			var result:String = '';
			while (true) {
				var remDiv:Long = rem.div(radixToPower);
				var intval:int = rem.subtract(remDiv.multiply(radixToPower)).toInt();
				var digits:String = Number(intval).toString(radix);

				rem = remDiv;
				if (rem.isZero()) {
					return digits + result;
				} else {
					while (digits.length < 6) digits = '0' + digits;
					result = '' + digits + result;
				}
			}
			return "";
		}

		public function not():Long { return Long.fromBits(~this.low_, ~this.high_); }

		public function modulo(other:Long):Long { return this.subtract(this.div(other).multiply(other)); }

		public function and(other:Long):Long { return Long.fromBits(this.low_ & other.low_, this.high_ & other.high_); }
		public function or(other:Long):Long { return Long.fromBits(this.low_ | other.low_, this.high_ | other.high_); }
		public function xor(other:Long):Long { return Long.fromBits(this.low_ ^ other.low_,  this.high_ ^ other.high_); }

		public function equals(other:Long):Boolean { return (this.high_ == other.high_) && (this.low_ == other.low_); }
		public function notEquals(other:Long):Boolean { return (this.high_ != other.high_) || (this.low_ != other.low_); }
		public function lessThan(other:Long):Boolean { return this.compare(other) < 0; }
		public function lessThanOrEqual(other:Long):Boolean { return this.compare(other) <= 0; }
		public function greaterThan(other:Long):Boolean { return this.compare(other) > 0; }
		public function greaterThanOrEqual(other:Long):Boolean { return this.compare(other) >= 0; }

		public function getNumBitsAbs():int {
			if (this.isNegative()) {
				if (this.equals(Long.MIN_VALUE)) {
					return 64;
				} else {
					return this.negate().getNumBitsAbs();
				}
			} else {
				var val:int = this.high_ != 0 ? this.high_ : this.low_;
				for (var bit:int = 31; bit > 0; bit--) {
					if ((val & (1 << bit)) != 0) {
						break;
					}
				}
				return this.high_ != 0 ? bit + 33 : bit + 1;
			}
		}

		public function shiftLeft(numBits:int):Long {
			numBits &= 63;
			if (numBits == 0) return this;
			var low:int = this.low_;
			if (numBits < 32) {
				var high:int = this.high_;
				return Long.fromBits(low << numBits, (high << numBits) | (low >>> (32 - numBits)));
			} else {
				return Long.fromBits(0, low << (numBits - 32));
			}
		}

		public function shiftRight(numBits:int):Long {
			numBits &= 63;
			if (numBits == 0) return this;
			var high:int = this.high_;
			if (numBits < 32) {
				var low:int = this.low_;
				return Long.fromBits((low >>> numBits) | (high << (32 - numBits)), high >> numBits);
			} else {
				return Long.fromBits(high >> (numBits - 32), high >= 0 ? 0 : -1);
			}
		}
		public function shiftRightUnsigned(numBits:int):Long {
			numBits &= 63;
			if (numBits == 0) return this;
			var high:int = this.high_;
			if (numBits < 32) {
				var low:int = this.low_;
				return Long.fromBits((low >>> numBits) | (high << (32 - numBits)),  high >>> numBits);
			} else if (numBits == 32) {
				return Long.fromBits(high, 0);
			} else {
				return Long.fromBits(high >>> (numBits - 32), 0);
			}
		}


		static public function neg(value:Long):Long { return value.negate(); }

		static public function cmp(l:Long, r:Long):int { return l.compare(r); }

		static public function add(l:Long, r:Long):Long { return l.add(r); }
		static public function sub(l:Long, r:Long):Long { return l.subtract(r); }

		static public function mul(l:Long, r:Long):Long { return l.multiply(r); }
		static public function div(l:Long, r:Long):Long { return l.div(r); }
		static public function mod(l:Long, r:Long):Long { return l.modulo(r); }

		static public function and(l:Long, r:Long):Long { return l.and(r);}
		static public function or(l:Long, r:Long):Long { return l.or(r); }
		static public function xor(l:Long, r:Long):Long { return l.xor(r); }

		static public function shl(l:Long, r:Long):Long { return l.shiftLeft(r.toInt()); }
		static public function shr(l:Long, r:Long):Long { return l.shiftRightUnsigned(r.toInt()); }
		static public function sar(l:Long, r:Long):Long { return l.shiftRight(r.toInt()); }

		static public function eq(l:Long, r:Long):Boolean { return l.equals(r); }
		static public function ne(l:Long, r:Long):Boolean { return l.notEquals(r); }
		static public function ge(l:Long, r:Long):Boolean { return l.greaterThanOrEqual(r); }
		static public function le(l:Long, r:Long):Boolean { return l.lessThanOrEqual(r); }
		static public function gt(l:Long, r:Long):Boolean { return l.greaterThan(r); }
		static public function lt(l:Long, r:Long):Boolean { return l.lessThan(r); }

	}
}
