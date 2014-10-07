package {
	import java.lang._Class;

	public class ObjectImpl {
		[Inline] static public function hashCode_(that:Object):int {
			return 0;
		}

		static public function getClass_(local6:*):java.lang._Class {
			return null;
		}

		[Inline] static public function equals_java_lang_Object(a:Object, b:Object):Boolean {
			return a == b;
		}

		[Inline] static public function toString_(that:Object):String {
			return String(that);
		}
	}
}