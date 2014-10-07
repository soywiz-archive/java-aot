package libgame.as3 {
	import flash.display.Bitmap;
	import flash.display.BitmapData;
	import flash.display.DisplayObjectContainer;
	import flash.display.Shape;
	import flash.display.Stage;

	import java.lang.Runnable;
	import flash.utils.setInterval;
	import libgame.as3.As3Native;
	import ObjectImpl;

	public class As3Native extends Object {
		public function __init__():void {
		}

		static public function init(root:DisplayObjectContainer):void {
			As3Native.stage = root.stage;
			As3Native.bitmapData = new BitmapData(640, 480);
			root.addChild(new Bitmap(As3Native.bitmapData));
		}

		static public function onEnterFrame_java_lang_Runnable(runnable:java.lang.Runnable):void {
			setInterval(runnable.run, 20);
		}

		static private var stage:Stage;
		static private var bitmapData:BitmapData;

		static public function clear_int(color:int):void {
			bitmapData.fillRect(bitmapData.rect, color);
		}

		static public function drawTriangles_int_float$Array(color:int, positions:Array):void {
			var shape:Shape = new Shape();
			shape.graphics.beginFill(color);

			var positionsV:Vector.<Number> = new Vector.<Number>();
			for (var n:int = 0; n < positions.length; n++) {
				positionsV[n] = positions[n];
			}
			shape.graphics.drawTriangles(positionsV);
			shape.graphics.endFill();
			bitmapData.draw(shape);
		}
	}
}
