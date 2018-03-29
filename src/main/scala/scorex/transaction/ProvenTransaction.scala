package scorex.transaction

import monix.eval.Coeval
import play.api.libs.json.{JsObject, Json}
import scorex.account.AddressScheme
import scorex.crypto.encode.Base58

trait ProvenTransaction extends Transaction with Proven {

  protected def proofField: (String, Json.JsValueWrapper) = "proofs" -> this.proofs.proofs.map(_.base58)

  val bodyBytes: Coeval[Array[Byte]]

  protected def jsonBase(implicit addressScheme: AddressScheme): JsObject =
    Json.obj(
      "type"            -> builder.typeId,
      "id"              -> id().base58,
      "sender"          -> sender.toAddress.address,
      "senderPublicKey" -> Base58.encode(sender.publicKey),
      "fee"             -> assetFee._2,
      "timestamp"       -> timestamp
    ) ++ Json.obj(proofField)
}
