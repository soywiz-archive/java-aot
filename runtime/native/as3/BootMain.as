package {
	import flash.display.Sprite;
	import flash.text.TextField;
	import flash.utils.setTimeout;

	import libcore._Native;

	/*!IMPORTS*/

	[SWF(width = 1280, height = 740, frameRate = 30)]
	public class BootMain extends flash.display.Sprite {
		public function BootMain() {
			setTimeout(main, 0);
		}

		private function main():void {
			addChild(_Native.consoleTextField = new TextField());
			_Native.consoleTextField.width = stage.stageWidth;
			_Native.consoleTextField.height = stage.stageHeight;
			var args:Array = [];
			/*!PREINIT*/
			/*!CALLMAIN*/
		}
	}
}