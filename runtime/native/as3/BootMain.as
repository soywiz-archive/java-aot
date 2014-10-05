package {
	import flash.display.Sprite;
	import flash.utils.setTimeout;
	import sample1.Sample1;

	public class BootMain extends flash.display.Sprite {
		public function BootMain() {
			setTimeout(main, 0);
		}

		private function main():void {
			Sample1.main_java_lang_String$Array([])
		}
	}
}