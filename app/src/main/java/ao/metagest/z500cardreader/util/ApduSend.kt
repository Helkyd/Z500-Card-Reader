package ao.metagest.z500cardreader.util

class ApduSend(Command: ByteArray, Lc: Short, DataIn: ByteArray, Le: Short) {
    var Command: ByteArray? = null
    var Lc: Short
    var DataIn: ByteArray? = null
    var Le: Short

    init {
        this.Command = ByteArray(Command.size)
        this.DataIn = ByteArray(DataIn.size)
        this.Command = Command
        this.Lc = Lc
        this.DataIn = DataIn
        this.Le = Le
    }

    val bytes: ByteArray
        get() {
            var index = 0
            val buf = ByteArray(520)
            if (Command != null) {
                System.arraycopy(Command, 0, buf, 0, Command!!.size)
            }
            index = Command!!.size
            buf[index++] = (Lc / 256).toByte()
            buf[index++] = (Lc % 256).toByte()
            if (DataIn != null) {
                System.arraycopy(DataIn, 0, buf, 6, DataIn!!.size)
            }
            //index +=DataIn.length;
            //buf[index++] = (byte) (Le / 256);
            //buf[index] = (byte) (Le % 256);
            return buf
        }
}