package {
	import flash.display.Sprite;
	import flash.text.TextField;
	import flash.utils.setTimeout;

	import libcore.Native;

	import sample1.Sample1;
	/*!IMPORTS*/

	public class BootMain extends flash.display.Sprite {
		public function BootMain() {
			setTimeout(main, 0);
		}

		private function main():void {
			addChild(Native.consoleTextField = new TextField());
			Native.consoleTextField.width = stage.stageWidth;
			Native.consoleTextField.height = stage.stageHeight;
			var args:Array = [];
			/*!PREINIT*/
			/*!CALLMAIN*/
		}
	}
}