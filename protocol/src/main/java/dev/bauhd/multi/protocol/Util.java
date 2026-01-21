package dev.bauhd.multi.protocol;

import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class Util {

  public static final int VERSION = 0;

  private Util() {
  }

  public static void writeVarInt(final ByteBuf buf, int value) {
    if ((value & (0xFFFFFFFF << 7)) == 0) {
      buf.writeByte((byte) value);
    } else if ((value & (0xFFFFFFFF << 14)) == 0) {
      buf.writeShort((short) ((value & 0x7F | 0x80) << 8 | (value >>> 7)));
    } else if ((value & (0xFFFFFFFF << 21)) == 0) {
      buf.writeMedium(
          (value & 0x7F | 0x80) << 16 | ((value >>> 7) & 0x7F | 0x80) << 8 | (value >>> 14));
    } else if ((value & (0xFFFFFFFF << 28)) == 0) {
      buf.writeInt((value & 0x7F | 0x80) << 24 | (((value >>> 7) & 0x7F | 0x80) << 16)
          | ((value >>> 14) & 0x7F | 0x80) << 8 | (value >>> 21));
    } else {
      buf.writeInt((value & 0x7F | 0x80) << 24 | ((value >>> 7) & 0x7F | 0x80) << 16
          | ((value >>> 14) & 0x7F | 0x80) << 8 | ((value >>> 21) & 0x7F | 0x80));
      buf.writeByte((byte) (value >>> 28));
    }
  }

  public static int readVarInt(final ByteBuf buf) {
    var b = buf.readByte();
    if ((b & 0x80) != 128) {
      return b;
    }
    var i = b & 0x7F;
    for (int j = 1; j < 5; j++) {
      b = buf.readByte();
      i |= (b & 0x7F) << j * 7;
      if ((b & 0x80) != 128) {
        return i;
      }
    }
    throw new RuntimeException("VarInt too big");
  }

  public static void writeUniqueId(final ByteBuf buf, final UUID value) {
    buf.writeLong(value.getMostSignificantBits());
    buf.writeLong(value.getLeastSignificantBits());
  }

  public static UUID readUniqueId(final ByteBuf buf) {
    return new UUID(buf.readLong(), buf.readLong());
  }

  public static void writeString(final ByteBuf buf, final String value) {
    writeVarInt(buf, encodedLength(value));
    buf.writeCharSequence(value, StandardCharsets.UTF_8);
  }

  public static String readString(final ByteBuf buf) {
    final var length = readVarInt(buf);
    return buf.readString(length, StandardCharsets.UTF_8);
  }

  private static int encodedLength(CharSequence sequence) {
    int utf16Length = sequence.length();
    int utf8Length = utf16Length;

    int i = 0;
    while (i < utf16Length && sequence.charAt(i) < 128) {
      i++;
    }

    while (i < utf16Length) {
      var c = sequence.charAt(i);
      if (c >= 2048) {
        utf8Length += encodedLengthGeneral(sequence, i);
        break;
      }
      utf8Length += 127 - c >>> 31;
      i++;
    }

    if (utf8Length < utf16Length) {
      var var6 = utf8Length + 4294967296L;
      throw new IllegalArgumentException("UTF-8 length does not fit in int: " + var6);
    } else {
      return utf8Length;
    }
  }

  private static int encodedLengthGeneral(CharSequence sequence, int start) {
    final var utf16Length = sequence.length();
    var utf8Length = 0;

    for (var i = start; i < utf16Length; ++i) {
      var c = sequence.charAt(i);
      if (c < 2048) {
        utf8Length += 127 - c >>> 31;
      } else {
        utf8Length += 2;
        if ('\ud800' <= c && c <= '\udfff') {
          if (Character.codePointAt(sequence, i) == c) {
            throw new IllegalArgumentException("Unpaired surrogate at index " + i);
          }
          ++i;
        }
      }
    }
    return utf8Length;
  }
}
