package jflash {
	import jflash.backend.EngineContext;
	import libgame.as3.As3EngineContext;

	public class EngineContextFactory {
		static public function create():EngineContext {
			var context:As3EngineContext = new As3EngineContext();
			context.__init__();
			return context;
		}
	}
}
