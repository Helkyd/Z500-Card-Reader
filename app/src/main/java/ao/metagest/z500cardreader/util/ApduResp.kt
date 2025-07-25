package ao.metagest.z500cardreader.util


class ApduResp {
    var lenOut: Short = 0
    var dataOut: ByteArray? = ByteArray(512)
    var sWA: Byte = 0
    var sWB: Byte = 0

    constructor()

    constructor(LenOut: Short, DataOut: ByteArray?, SWA: Byte, SWB: Byte) {
        this.lenOut = LenOut
        this.dataOut = DataOut
        this.sWA = SWA
        this.sWB = SWB
    }

    constructor(resp: ByteArray) {
        this.lenOut = ((resp[1].toInt() and 0xff) * 256 + (resp[0].toInt() and 0xff)).toShort()
        System.arraycopy(resp, 2, this.dataOut, 0, 512)
        this.sWA = resp[514]
        this.sWB = resp[515]
    }
}
