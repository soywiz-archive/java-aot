package {
	import flash.display.Sprite;
	import flash.text.TextField;
	import flash.utils.setTimeout;

	import libcore.Native;

	import libgame.as3.As3Native;

	/*!IMPORTS*/

	[SWF(width = 1280, height = 740, frameRate = 60)]
	public class BootMain extends flash.display.Sprite {
		public function BootMain() {
			setTimeout(main, 0);
		}

		private function main():void {
			/*!PREINIT*/

			As3Native.init(this, function():void {
				var args:Array = [];
				/*!CALLMAIN*/
			});
		}
	}
}