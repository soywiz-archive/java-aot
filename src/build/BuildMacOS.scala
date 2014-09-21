package build

import java.io._
import java.nio.charset.Charset
import javax.imageio.ImageIO

import com.google.common.io.{ByteStreams, Files}

import scala.io.Source

object BuildMacOS {
  // Contents/Resources/app.icns
  // Contents/PkgInfo ---- APPL????
  // Contents/MacOS/executable
  // Contents/Frameworks
  // Contents/Info.plist

  def createAPP(path:String, name:String, executable:Array[Byte], png512x512:Array[Byte]): Unit = {
    new File(s"$path/Contents/Resources").mkdirs()
    new File(s"$path/Contents/MacOS").mkdirs()
    new File(s"$path/Contents/Frameworks").mkdirs()
    Files.write(executable, new File(s"$path/Contents/MacOS/app"))
    Runtime.getRuntime.exec(s"chmod +x $path/Contents/MacOS/app")

    Files.write(createIcns(png512x512), new File(s"$path/Contents/Resources/app.icns"))
    //Files.write(icns, new File(s"$path/Contents/Resources/app.icns"))
    Files.write("APPL????", new File(s"$path/Contents/PkgInfo"), Charset.forName("UTF-8"))
    Files.write(
    s"""
      |<?xml version="1.0" encoding="UTF-8"?>
      |<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
      |<plist version="1.0">
      |<dict>
      |	<key>CFBundleExecutable</key>
      |	<string>app</string>
      |	<key>CFBundleGetInfoString</key>
      |	<string>0.1, 2014-Jul-08, App Foundation</string>
      |	<key>CFBundleIconFile</key>
      |	<string>app.icns</string>
      |	<key>CFBundleIdentifier</key>
      |	<string>com.soywiz.sample1</string>
      |	<key>CFBundleInfoDictionaryVersion</key>
      |	<string>1.0</string>
      |	<key>CFBundleName</key>
      |	<string>$name</string>
      |	<key>CFBundlePackageType</key>
      |	<string>APPL</string>
      |	<key>CFBundleShortVersionString</key>
      |	<string>1.0</string>
      |	<key>CFBundleSignature</key>
      |	<string>????</string>
      |	<key>CFBundleVersion</key>
      |	<string>0.1, 2014-Jul-08, App Foundation</string>
      |	<key>NSPrincipalClass</key>
      |	<string>NSApplication</string>
      |	<key>NSHighResolutionCapable</key>
      |	<true/>
      |</dict>
      |</plist>
    """.stripMargin
    , new File(s"$path/Contents/Info.plist"), Charset.forName("UTF-8"))
  }

  private def createIcns(png512x512:Array[Byte]) = {
    val outPath = System.getProperty("java.io.tmpdir") + "/build_iconset"
    val outPathSet = s"$outPath/app.iconset"
    new File(outPathSet).mkdirs()
    Files.write(png512x512, new File(s"$outPath/app.png"))

    def exec(command:String) = {
      println(command)
      val p = Runtime.getRuntime.exec(command)
      for (line <- Source.fromInputStream(p.getInputStream).getLines()) {
        println(line)
      }

      for (line <- Source.fromInputStream(p.getErrorStream).getLines()) {
        println(line)
      }
      p.waitFor()
    }

    //for (size <- List(16, 32, 128, 256, 512)) {
    for (size <- List(16, 256)) {
      exec(s"sips -z ${size} ${size}         ${outPath}/app.png --out $outPathSet/icon_${size}x${size}.png")
      //exec(s"sips -z ${size * 2} ${size * 2} ${outPath}/app.png --out $outPathSet/icon_${size}x${size}@2x.png")
    }

    exec(s"iconutil -c icns -o ${outPath}/app.icns ${outPathSet}")

    Files.toByteArray(new File(s"${outPath}/app.icns"))


    /*
mkdir MyIcon.iconset
sips -z 16 16     Icon1024.png --out MyIcon.iconset/icon_16x16.png
sips -z 32 32     Icon1024.png --out MyIcon.iconset/icon_16x16@2x.png
sips -z 32 32     Icon1024.png --out MyIcon.iconset/icon_32x32.png
sips -z 64 64     Icon1024.png --out MyIcon.iconset/icon_32x32@2x.png
sips -z 128 128   Icon1024.png --out MyIcon.iconset/icon_128x128.png
sips -z 256 256   Icon1024.png --out MyIcon.iconset/icon_128x128@2x.png
sips -z 256 256   Icon1024.png --out MyIcon.iconset/icon_256x256.png
sips -z 512 512   Icon1024.png --out MyIcon.iconset/icon_256x256@2x.png
sips -z 512 512   Icon1024.png --out MyIcon.iconset/icon_512x512.png
cp Icon1024.png MyIcon.iconset/icon_512x512@2x.png
iconutil -c icns MyIcon.iconset
rm -R MyIcon.iconset
     */
    /*
    val os = new ByteArrayOutputStream()
    val dos = new DataOutputStream(os)
    dos.writeBytes("icns")
    dos.writeInt(4 + 4 + 4 + png512x512.length)
    dos.writeBytes("ic09") // 512×512
    dos.writeInt(png512x512.length)
    dos.write(png512x512)
    os.toByteArray
    */
  }
}

/*

 */