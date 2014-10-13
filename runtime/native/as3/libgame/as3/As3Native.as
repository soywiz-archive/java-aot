package libgame.as3 {
	import flash.display.Bitmap;
	import flash.display.BitmapData;
	import flash.display.DisplayObjectContainer;
	import flash.display.Shape;
	import flash.display.Stage;
	import flash.display3D.Context3D;
	import flash.display3D.Context3DProgramType;
	import flash.display3D.Context3DVertexBufferFormat;
	import flash.display3D.IndexBuffer3D;
	import flash.display3D.Program3D;
	import flash.display3D.VertexBuffer3D;
	import flash.events.Event;
	import flash.geom.Matrix3D;
	import flash.geom.Vector3D;
	import flash.utils.getTimer;

	import java.lang.Runnable;
	import flash.utils.setInterval;

	import libcore.Native;

	import libgame.as3.As3Native;
	import ObjectImpl;

	public class As3Native extends Object {
		static private var context3D:Context3D;
		static private var program:Program3D;
		static private var vertexbuffer:VertexBuffer3D;
		static private var indexbuffer:IndexBuffer3D;

		public function __init__():void {
		}

		static public function init(root:DisplayObjectContainer, done:Function):void {
			var stage:Stage = root.stage;
			As3Native.stage = root.stage;

			stage.stage3Ds[0].addEventListener( Event.CONTEXT3D_CREATE, function(e:Event = null):void {
				init2();
				/*
				var context3D:Context3D = stage.stage3Ds[0].context3D;
				context3D.configureBackBuffer(800, 600, 2, true);

				As3Native.bitmapData = new BitmapData(640, 480);
				root.addChild(new Bitmap(As3Native.bitmapData));
				*/
				done();
			});
			stage.stage3Ds[0].requestContext3D();

			//root.addChild(Native.consoleTextField = new TextField());
			//Native.consoleTextField.width = stage.stageWidth;
			//Native.consoleTextField.height = stage.stageHeight;
		}

		static private function init2():void {
			context3D = stage.stage3Ds[0].context3D;
			context3D.configureBackBuffer(stage.stageWidth, stage.stageHeight, 0, false);

			var vertexShaderAssembler : AGALMiniAssembler = new AGALMiniAssembler();
			vertexShaderAssembler.assemble( Context3DProgramType.VERTEX,
				"m44 op, va0, vc0\n" + // pos to clipspace
				"mov v0, va1" // copy color
			);

			var fragmentShaderAssembler : AGALMiniAssembler= new AGALMiniAssembler();
			fragmentShaderAssembler.assemble( Context3DProgramType.FRAGMENT,

				"mov oc, v0"
			);

			program = context3D.createProgram();
			program.upload( vertexShaderAssembler.agalcode, fragmentShaderAssembler.agalcode);
		}

		static public function onEnterFrame_java_lang_Runnable(runnable:java.lang.Runnable):void {
			As3Native.stage.addEventListener("enterFrame", function(e:*):void {
				runnable.run();
			});
		}

		static private var stage:Stage;
		//static private var bitmapData:BitmapData;

		static public function clear_int(color:int):void {
			context3D.clear(1, 1, 1, 1);
			//bitmapData.fillRect(bitmapData.rect, color);
		}

		static public function present():void {
			context3D.present();
		}

		static public function drawTriangles_int_float$Array(color:int, positions:Vector.<Number>):void {
			if (vertexbuffer) vertexbuffer.dispose();
			if (indexbuffer) indexbuffer.dispose();

			// x, y, z, r, g, b
			var vertices:Vector.<Number> = new Vector.<Number>(0);

			var vertexCount:int = positions.length / 2;
			var indices:Vector.<uint> = Vector.<uint>([]);
			for (var n:int = 0; n < vertexCount; n++) {
				indices.push(n);
				vertices.push(positions[n * 2 + 0] / 800 - 1);
				vertices.push((positions[n * 2 + 1] / 600 - 1) * -1);
				vertices.push(0);
				vertices.push(1);
				vertices.push(0);
				vertices.push(0);
			}

			vertexbuffer = context3D.createVertexBuffer(vertexCount, 6);
			vertexbuffer.uploadFromVector(vertices, 0, vertexCount);

			indexbuffer = context3D.createIndexBuffer(vertexCount);
			indexbuffer.uploadFromVector(indices, 0, vertexCount);

			context3D.setVertexBufferAt(0, vertexbuffer, 0, Context3DVertexBufferFormat.FLOAT_3);
			context3D.setVertexBufferAt(1, vertexbuffer, 3, Context3DVertexBufferFormat.FLOAT_3);
			context3D.setProgram(program);

			var m:Matrix3D = new Matrix3D();
			//m.appendRotation(getTimer()/40, Vector3D.Z_AXIS);
			context3D.setProgramConstantsFromMatrix(Context3DProgramType.VERTEX, 0, m, true);
			context3D.drawTriangles(indexbuffer);
		}
	}
}
