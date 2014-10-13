package flash.display3d;

import flash.display.BitmapData;
import flash.display3d.textures.CubeTexture;
import flash.display3d.textures.RectangleTexture;
import flash.display3d.textures.Texture;
import flash.geom.Matrix3D;
import flash.geom.Rectangle;
import flash.utils.ByteArray;
import jflash.backend.TextureBase;
import libcore.NativeClass;

@NativeClass
public class Context3D {
    public String driverInfo;
    public boolean enableErrorChecking;
    public String profile;
    public int backBufferWidth;
    public int backBufferHeight;
    public int maxBackBufferWidth;
    public int maxBackBufferHeight;

    native public void dispose(boolean recreate);
    native public void configureBackBuffer(int width, int height, int antiAlias, boolean enableDepthAndStencil/* = true*/, boolean wantsBestResolution/* = false*/, boolean wantsBestResolutionOnBrowserZoom/* = false*/);

    native public void clear(double red/* = 0.0*/, double green/* = 0.0*/, double blue/* = 0.0*/, double alpha/* = 1.0*/, double depth/* = 1.0*/, int stencil/* = 0*/, int mask/* = NaN*/);
    native public void drawTriangles(IndexBuffer3D indexBuffer, int firstIndex/* = 0*/, int numTriangles/* = -1*/);
    native public void present();
    native public void setProgram(Program3D program);
    native public void setProgramConstantsFromVector(String programType, int firstRegister, double[] data, int numRegisters/* = -1*/);

    native public void setProgramConstantsFromMatrix(String programType, int firstRegister, Matrix3D matrix, boolean transposedMatrix/* = false*/);

    native public void setProgramConstantsFromByteArray(String programType, int firstRegister, int numRegisters, ByteArray data, int byteArrayOffset);
    native public void setVertexBufferAt(int index,VertexBuffer3D buffer, int bufferOffset/* = 0*/, String format/* = "float4"*/);
    native public void setBlendFactors(String sourceFactor, String destinationFactor);
    native public void setColorMask(boolean red, boolean green, boolean blue, boolean alpha);

    native public void setDepthTest(boolean depthMask, String passCompareMode);
    native public void setTextureAt(int sampler, TextureBase texture);
    native public void setRenderToTexture(TextureBase texture, boolean enableDepthAndStencil/* = false*/, int antiAlias/* = 0*/, int surfaceSelector/* = 0*/, int colorOutputIndex/* = 0*/);
    native public void setRenderToBackBuffer();
    native private void setRenderToTextureInternal(TextureBase texture, int targetType, boolean enableDepthAndStencil, int antiAlias, int surfaceSelector, int colorOutputIndex);

    native public void setCulling(String triangleFaceToCull);
    native public void setStencilActions(String triangleFace/* = "frontAndBack"*/, String compareMode/* = "always"*/, String actionOnBothPass/* = "keep"*/, String actionOnDepthFail/* = "keep"*/, String actionOnDepthPassStencilFail/* = "keep"*/);
    native public void setStencilReferenceValue(int referenceValue, int readMask/* = 0xFF*/, int writeMask/* = 0xFF*/);
    native public void setScissorRectangle(Rectangle rectangle);
    native public VertexBuffer3D createVertexBuffer(int numVertices, int data32PerVertex, String bufferUsage/* = "staticDraw"*/);

    native public IndexBuffer3D createIndexBuffer(int numIndices, String bufferUsage/* = "staticDraw"*/);
    native public Texture createTexture(int width, int height, String format, boolean optimizeForRenderToTexture, int streamingLevels/* = 0*/);
    native public CubeTexture createCubeTexture(int size, String format, boolean optimizeForRenderToTexture, int streamingLevels/* = 0*/);
    native public RectangleTexture createRectangleTexture(int width, int height, String format, boolean optimizeForRenderToTexture);
    native public Program3D createProgram();

    native public void drawToBitmapData(BitmapData destination);
    native public void setSamplerStateAt(int sampler, String wrap, String filter, String mipfilter);

    native private void setTextureInternal(int sampler, Texture texture);
    native private void setCubeTextureInternal(int sampler, CubeTexture texture);

    native private void setRectangleTextureInternal(int sampler, RectangleTexture texture);

}
