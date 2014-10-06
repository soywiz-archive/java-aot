package {
	import java.lang.String;

	public class JavaCore {
		static public function lstring(str:*):java.lang.String {
			var out:java.lang.String = new java.lang.String();
			var array:Array = [];
			for (var n:int = 0; n < str.length; n++) array.push(str.charCodeAt(n));
			out.__init___char$Array(array);
			return out;
		}
	}
}