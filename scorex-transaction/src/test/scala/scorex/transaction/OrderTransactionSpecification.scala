package scorex.transaction

import org.scalatest._
import org.scalatest.prop.PropertyChecks
import scorex.account.PublicKeyAccount
import scorex.transaction.exchange.Order
import scorex.utils.NTP

class OrderTransactionSpecification extends PropSpec with PropertyChecks with Matchers with TransactionGen {

  property("Order transaction serialization roundtrip") {
    forAll(orderGenerator) { order: Order =>
      val recovered = Order.parseBytes(order.bytes).get
      recovered.bytes shouldEqual order.bytes
    }
  }

  property("Order generator should generate valid orders") {
    forAll(orderGenerator) { order: Order =>
      order.isValid shouldBe true
    }
  }

  property("Order timestamp validation") {
    forAll(invalidOrderGenerator) { order: Order =>
      val isValid = order.isValid
      val time = NTP.correctedTime()
      whenever(order.maxTimestamp < time || order.maxTimestamp > time + Order.MaxLiveTime) {
        isValid shouldBe false
      }
    }
  }

  property("Order amount validation") {
    forAll(invalidOrderGenerator) { order: Order =>
      whenever(order.amount <= 0) {
        order.isValid shouldBe false
      }
    }
  }

  property("Order matcherFee validation") {
    forAll(invalidOrderGenerator) { order: Order =>
      whenever(order.matcherFee <= 0) {
        order.isValid shouldBe false
      }
    }
  }

  property("Order price validation") {
    forAll(invalidOrderGenerator) { order: Order =>
      whenever(order.price <= 0) {
        order.isValid shouldBe false
      }
    }
  }

  property("Order signature validation") {
    forAll(orderGenerator, bytes32gen) { (order: Order, bytes: Array[Byte]) =>
      order.isValid shouldBe true
      order.copy(sender = new PublicKeyAccount(bytes)).isValid shouldBe false
      order.copy(matcher = new PublicKeyAccount(bytes)).isValid shouldBe false
      order.copy(spendAssetId = Array(0: Byte) ++ order.spendAssetId).isValid shouldBe false
      order.copy(receiveAssetId = Array(0: Byte) ++ order.receiveAssetId).isValid shouldBe false
      order.copy(price = order.price + 1).isValid shouldBe false
      order.copy(amount = order.amount + 1).isValid shouldBe false
      order.copy(maxTimestamp = order.maxTimestamp + 1).isValid shouldBe false
      order.copy(matcherFee = order.matcherFee + 1).isValid shouldBe false
      order.copy(signature = bytes ++ bytes).isValid shouldBe false
    }
  }


}
