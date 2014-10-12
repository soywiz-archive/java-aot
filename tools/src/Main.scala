import java.io._
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.zip.{InflaterInputStream, Inflater, DeflaterInputStream, ZipFile}

import com.sun.org.glassfish.gmbal.IncludeSubclass

object Main extends App {
  val swc = new ZipFile(new File("/Developer/airsdk15/frameworks/libs/player/15.0/playerglobal.swc"))
  val is = swc.getInputStream(swc.getEntry("library.swf"))
  val disDI = new DataInputStream(is)
  val dis = new LittleEndianDataInputStream(is)
  var compressed = false
  dis.readUnsignedByte.toChar match {
    case 'C' => compressed = true
    case 'F' => compressed = false
    case 'Z' => throw new Exception
  }
  if (dis.readUnsignedByte.toChar != 'W') throw new Exception
  if (dis.readUnsignedByte.toChar != 'S') throw new Exception
  val version = dis.readUnsignedByte
  val uncompressedSize = dis.readInt()
  //println()



  val data = if (compressed) {
    StreamUtils.streamRead(new InflaterInputStream(disDI), uncompressedSize - 8)
  } else {
    StreamUtils.streamRead(disDI, uncompressedSize - 8)
  }

  val tagsis = new ByteArrayInputStream(data)
  val swfs = new SWFStreamReader(tagsis)
  //val fos = new FileOutputStream("/Developer/airsdk15/frameworks/libs/player/15.0/playerglobal.bin")
  //fos.write(data)



  val size = swfs.readRECT()
  val fps = swfs.readFIXED88()
  val frameCount = swfs.readU16()
  println(size)
  println(fps)
  println(frameCount)

  def readTags(tagHandler: (Int, Array[Byte]) => Unit) = {
    while (tagsis.available() > 0) {
      val tagHeader = swfs.readU16()
      val tagType = BitUtils.extract(tagHeader, 6, 10)
      var tagLength = BitUtils.extract(tagHeader, 0, 6)
      if (tagLength == 0x3F) {
        tagLength = swfs.readU32()
      }

      val tagContent = StreamUtils.streamRead(tagsis, tagLength)

      tagHandler(tagType, tagContent)
    }
  }

  /*
    u16 minor_version
    u16 major_version
    cpool_info constant_pool
    u30 method_count
    method_info method[method_count]
    u30 metadata_count
    metadata_info metadata[metadata_count]
    u30 class_count
    instance_info instance[class_count]
    class_info class[class_count]
    u30 script_count
    script_info script[script_count]
    u30 method_body_count
    method_body_info method_body[method_body_count]


    u30 int_count
    s32 integer[int_count]
    u30 uint_count
    u32 uinteger[uint_count]
    u30 double_count
    d64 double[double_count]
    u30 string_count
    string_info string[string_count]
    u30 namespace_count
    namespace_info namespace[namespace_count]
    u30 ns_set_count
    ns_set_info ns_set[ns_set_count]
    u30 multiname_count
    multiname_info multiname[multiname_count]

    method_info
    {
    u30 param_count
    u30 return_type
    u30 param_type[param_count]
    u30 name
    u8  flags
    option_info options
    param_info param_names
    }

    option_info
    {
      u30 option_count
      option_detail option[option_count]
    }
    option_detail
    {
      u30 val
      u8  kind
    }

    param_info {
      u30 param_name[param_count]
    }

    NEED_ARGUMENTS 0x01
    NEED_ACTIVATION 0x02
    NEED_REST 0x04
    HAS_OPTIONAL 0x08
    SET_DXNS 0x40
    HAS_PARAM_NAMES 0x80
    Meaning
    Suggests to the run-time that an “arguments” object (as specified by the ActionScript 3.0 Language Reference) be created. Must not be used together with NEED_REST. See Chapter 3.
    Must be set if this method uses the newactivation opcode.
    This flag creates an ActionScript 3.0 rest arguments array. Must not be
    used with NEED_ARGUMENTS. See Chapter 3.
    Must be set if this method has optional parameters and the options
    field is present in this method_info structure.
    Must be set if this method uses the dxns or dxnslate opcodes.
    Must be set when the param_names field is present in this method_info structure.

  instance_info {
    u30 name
    u30 super_name
    u8  flags
    u30 protectedNs
    u30 intrf_count
    u30 interface[intrf_count]
    u30 iinit
    u30 trait_count
    traits_info trait[trait_count]
  }

  traits_info
  {
    u30 name
    u8  kind
    u8  data[]
    u30 metadata_count
    u30 metadata[metadata_count]
  }

  trait_slot {
      u30 slot_id
      u30 type_name
      u30 vindex
      u8  vkind
  }
  */

  def readABC(abc:SWFStreamReader) = {
    var ints:Array[Int] = null
    var uints:Array[Int] = null
    var doubles:Array[Double] = null
    var strings:Array[String] = null
    var namespaces:Array[Namespace] = null
    var nsset:Array[Any] = null
    var multinames:Array[MultiName] = null
    var methods:Array[Method] = null

    def readSTRING_INFO() = {
      val size = abc.readU30()
      abc.bits.byteBound()
      new String(StreamUtils.streamRead(abc.is, size), "UTF-8")
    }

    def readMULTINAME_INFO(): MultiName = {
      val kind = abc.readU8()
      kind match {
        case 0x07 | 0x0D => ConstantQName(namespaces(abc.readU30()), strings(abc.readU30()))
        case 0x09 | 0x0E => ConstantMultiname(strings(abc.readU30()), nsset(abc.readU30()))
        case 0x0F | 0x10 => ConstantRTQName(strings(abc.readU30()))
        case 0x11 | 0x12 => ConstantRTQNameL()
        case 0x1B | 0x1C => ConstantMultinameL(nsset(abc.readU30()))

        case 0x1D | 0x1E => ConstantUnknown(abc.readU30(), abc.readU30(), abc.readU30())
      }
    }

    def readNAMESPACE_INFO() = {
      Namespace(abc.readU8(), strings(abc.readU30()))

      //CONSTANT_Namespace                  0x08 - 8
      //CONSTANT_PackageNamespace           0x16 - 22
      //CONSTANT_PackageInternalNs          0x17 - 23
      //CONSTANT_ProtectedNamespace         0x18 - 24
      //CONSTANT_ExplicitNamespace          0x19 - 25
      //CONSTANT_StaticProtectedNs          0x1A - 26
      //CONSTANT_PrivateNs                  0x05 - 5
    }

    def readNS_SET_INFO() = for (n <- 0 until abc.readU30()) yield abc.readU30()

    def readConstantPool(): Unit = {
      ints = (0 :: (for (v <- 0 until abc.readU30() - 1) yield abc.readVarInt()).toList).toArray
      uints = (0 :: (for (v <- 0 until abc.readU30() - 1) yield abc.readVarInt()).toList).toArray
      doubles = (0.0 :: (for (v <- 0 until abc.readU30() - 1) yield abc.readD64()).toList).toArray
      strings = ("" :: (for (v <- 0 until abc.readU30() - 1) yield readSTRING_INFO()).toList).toArray
      namespaces = (null :: (for (v <- 0 until abc.readU30() - 1) yield readNAMESPACE_INFO()).toList).toArray
      nsset = (null :: (for (v <- 0 until abc.readU30() - 1) yield readNS_SET_INFO()).toList).toArray
      multinames = (ConstantRTQName("*") :: (for (v <- 0 until abc.readU30() - 1) yield readMULTINAME_INFO()).toList).toArray
    }

    case class Method(paramCount:Int, returnType:MultiName, name:String, flags:Int, options:List[(Int, Int)], paramTypes:List[MultiName], paramNames:List[String])

    def readMethods(): Unit = {
      def readMethod(): Method = {
        val paramCount = abc.readU30()
        val returnType = multinames(abc.readU30())
        val paramTypes = for (m <- 0 until paramCount) yield multinames(abc.readU30())
        val name = strings(abc.readU30())
        //println(s"name:$name:" + strings(name))
        val flags = abc.readU8()
        val NEED_ARGUMENTS = (flags & 1) != 0
        val NEED_ACTIVATION = (flags & 2) != 0
        val NEED_REST = (flags & 4) != 0
        val HAS_OPTIONAL = (flags & 8) != 0
        val SET_DXNS = (flags & 0x40) != 0
        val HAS_PARAM_NAMES = (flags & 0x80) != 0

        val options = if (HAS_OPTIONAL) {
          for (m <- 0 until abc.readU30()) yield (abc.readU30(), abc.readU8())
        } else {
          List()
        }
        val paramNames = if (HAS_PARAM_NAMES) {
          for (m <- 0 until paramCount) yield strings(abc.readU30())
        } else {
          List()
        }

        Method(paramCount, returnType, name, flags, options.toList, paramTypes.toList, paramNames.toList)
      }

      methods = (for (n <- 0 until abc.readU30()) yield readMethod()).toArray
    }

    def readMetadatas(): Unit = {
      def readItems() = for (m <- 0 until abc.readU30()) yield (strings(abc.readU30()), strings(abc.readU30()))

      for (n <- 0 until abc.readU30()) {
        val name = strings(abc.readU30())
        val items = readItems()
      }
    }

    def readInstances(): Unit = {
      def readTrait(): Unit = {
        val name = multinames(abc.readU30())
        println(s"trait:$name")
        val kind = abc.readU8()
        val kind_kind = BitUtils.extract(kind, 0, 4)
        val kind_flags = BitUtils.extract(kind, 4, 4)
        // ATTR_Final 0x1 ATTR_Override 0x2 ATTR_Metadata 0x4

        val data = kind_kind match {
          // Trait_Slot 0 Trait_Method 1 Trait_Getter 2 Trait_Setter 3 Trait_Class 4 Trait_Function 5 Trait_Const 6
          case 0 | 6 =>
            val slot_id = abc.readU30()
            val type_name = abc.readU30()
            val vindex = abc.readU30()
            val vkind = abc.readU8()
            (slot_id, type_name, vindex, vkind)
          case 1 | 2 | 3 =>
            val disp_id = abc.readU30()
            val method = abc.readU30()
            println(method)
            println(methods(method))
            (disp_id, method)
          case 4 =>
            val slot_id = abc.readU30()
            val classi = abc.readU30()
            (slot_id, classi)
          case 5 =>
            val slot_id = abc.readU30()
            val function = abc.readU30()
            (slot_id, function)
          case 7 =>
            val slot_id = abc.readU30()
            val classi = abc.readU30()
            (slot_id, classi)
        }
        val isFinal = (kind_flags & 1) != 0
        val isOverride = (kind_flags & 2) != 0
        val metadata = if ((kind_flags & 0x4) != 0) {
          for (m <- 0 until abc.readU30()) yield abc.readU30()
        } else {
          List()
        }
        (name, kind, data, metadata)
      }

      def readInstanceInfo(): Unit = {
        println("------------------------------")
        val name = multinames(abc.readU30())
        println(s"name:$name")
        val super_name = multinames(abc.readU30())
        println(s"super_name:$super_name")
        val flags = abc.readU8()
        println(s"flags:$flags")
        val protected_ns = namespaces(abc.readU30())
        println(s"protected_ns:$protected_ns")
        var interfaces = for (m <- 0 until abc.readU30()) yield strings(abc.readU30())
        println(s"interfaces:$interfaces")
        val iinit = methods(abc.readU30())
        println(s"iinit:$iinit")
        var traits = for (m <- 0 until abc.readU30()) yield readTrait()
      }

      def readClassInfo(): Unit = {
        val cinit = methods(abc.readU30())
        var traits = for (m <- 0 until abc.readU30()) yield readTrait()
        println(cinit)
        (cinit, traits)
      }

      val class_count = abc.readU30()
      val instanceInfos = for (n <- 0 until class_count) yield readInstanceInfo()
      val classInfos = for (n <- 0 until class_count) yield readClassInfo()
    }

    val minor_version = abc.readU16()
    val major_version = abc.readU16()
    println(s"$minor_version, $major_version")
    readConstantPool()
    readMethods()
    readMetadatas()
    readInstances()
  }

  readTags((tagType, data) => {
    //println(s"$tagType, ${data.length}")
    tagType match {
        // DoABC
      case 82 =>
        val abc = new SWFStreamReader(new ByteArrayInputStream(data))
        val flags = abc.readU32()
        val name = abc.readSTRING()
        println(s"doABC: flags:$flags, name:$name")
        readABC(abc)
        //println()
      case _ =>
    }
  })


  //println(s"RECT(($xmin,$ymin)-($xmax,$ymax))")
  //println(deflater.read)

  /*
  val bis = new BitInputStream(is)
  println(bis.read().toChar)
  println(bis.read().toChar)
  println(bis.read().toChar)
  */
}

object StreamUtils {
  def streamRead(is:InputStream, _count:Int):Array[Byte] = {
    var count = _count
    val out = new Array[Byte](count)
    var offset = 0
    while (count > 0) {
      val readed = is.read(out, offset, count)
      offset += readed
      count -= readed
    }
    out
  }
}

class SWFStreamReader(val is:InputStream) {
  val bits = new BitInputStream(is)

  def readBytes(count:Int) = {
    bits.byteBound()
    StreamUtils.streamRead(is, count)
  }

  def readRECT() = {
    val nbits = bits.readBits(5)
    val xmin = bits.readBits(nbits) / 20
    val xmax = bits.readBits(nbits) / 20
    val ymin = bits.readBits(nbits) / 20
    val ymax = bits.readBits(nbits) / 20
    (xmin, ymin, xmax, ymax)
  }

  def readFIXED88() = {
    bits.byteBound()
    val low = bits.readBits(8)
    val high = bits.readBits(8)
    (high, low)
  }

  def readU8() = {
    bits.byteBound()
    bits.readBits(8)
  }

  def readU16() = {
    val low = readU8()
    val high = readU8()
    (high << 8) | low
  }

  def readU32() = {
    val low = readU16()
    val high = readU16()
    (high << 16) | low
  }

  def readS32() = {
    readU32()
  }

  def readVarInt():Int = {
    var out = 0
    var offset = 0
    var v = 0
    do {
      v = readU8()
      //println(v)
      out |= (v & 0x7f) << offset
      offset += 7
    } while ((v & 0x80) != 0)
    out
  }

  def readU30() = {
    readVarInt()
  }

  def readU64() = {
    val low = readU32().toLong
    val high = readU32().toLong
    (high << 32) | low
  }

  def readD64() = {
    java.lang.Double.longBitsToDouble(readU64())
  }

  def readSTRING() = {
    var c:Byte = 0
    bits.byteBound()
    val bout = new ByteArrayOutputStream()
    do {
      c = bits.readBits(8).toByte
      if (c != 0) bout.write(c)
    } while (c != 0)
    new String(bout.toByteArray, "UTF-8")
  }
}

case class Namespace(kind:Int, name:String)

abstract class MultiName()
case class ConstantQName(ns:Namespace, name:String) extends MultiName
case class ConstantRTQName(name:String) extends MultiName
case class ConstantRTQNameL() extends MultiName
case class ConstantMultiname(name:String, ns_set:Any) extends MultiName
case class ConstantMultinameL(ns_set:Any) extends MultiName
case class ConstantUnknown(a:Int = 0, b:Int = 0, c:Int = 0) extends MultiName

object BitUtils {
  @inline def mask(len:Int) = (1 << len) - 1
  def extract(v:Int, offset:Int, len:Int) = (v >> offset) & mask(len)
  def swap32(v:Int) = {
    (((v >>> 24) & 0xFF) << 0) |
    (((v >>> 16) & 0xFF) << 8) |
    (((v >>> 8) & 0xFF) << 16) |
    (((v >>> 0) & 0xFF) << 24)
  }
}