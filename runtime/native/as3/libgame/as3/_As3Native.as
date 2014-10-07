package libgame.as3 {
	import java.lang._Runnable;
	import flash.utils.setInterval;
	import libgame.as3._As3Native;
	import ObjectImpl;

	public class _As3Native extends Object {
		public function __init___():void {
		}

		static public function onEnterFrame_java_lang_Runnable(runnable:java.lang._Runnable):void {
			setInterval(runnable.run_, 20);
		}
	}
}
