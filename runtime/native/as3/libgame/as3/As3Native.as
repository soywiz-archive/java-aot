package libgame.as3 {
	import java.lang.Runnable;
	import flash.utils.setInterval;
	import libgame.as3.As3Native;
	import ObjectImpl;

	public class As3Native extends Object {
		public function __init__():void {
		}

		static public function onEnterFrame_java_lang_Runnable(runnable:java.lang.Runnable):void {
			setInterval(runnable.run, 20);
		}
	}
}
