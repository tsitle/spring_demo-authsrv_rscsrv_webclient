package com.ts.springdemo.authserver.entity

import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.util.Base64URL
import com.ts.springdemo.authserver.service.EncryptionService
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.util.Assert
import java.io.Serializable
import java.math.BigInteger


@Suppress("unused")
@Document(collection = "rsaKey")
class DbJwkRsaKey private constructor() : Serializable {
	private var id: String = ""
	private var checksum: String = ""

	private var plainRsaPub: MutableMap<String, String> = mutableMapOf()
	private var encrRsaPriv: MutableMap<String, String> = mutableMapOf()

	fun getId(): String {
		return id
	}

	fun getRsaKey(pw: String): RSAKey {
		Assert.hasText(pw, "pw cannot be empty")
		Assert.hasText(checksum, "checksum cannot be empty")

		val plainRsaPriv: MutableMap<String, String> = mutableMapOf()
		encrRsaPriv.forEach { (itKey: String, itVal: String) ->
				Assert.hasText(itVal, "EncrRsaPriv '$itKey' cannot be empty")
				plainRsaPriv[itKey] = decryptField(pw, itVal)
			}

		val checksumExp = decryptField(pw, checksum)
		val checksumIs = getChecksumForRsaFields(plainRsaPub, plainRsaPriv)
		if (checksumIs != checksumExp) {
			throw IllegalStateException("checksum of DbJwkRsaKey ID '$id' does not match")
		}

		return RSAKey.Builder(
						Base64URL.from(plainRsaPub["Modulus"]),
						Base64URL.from(plainRsaPub["Exp"])
					)
				.keyID(plainRsaPriv["Id"])
				.privateExponent(
						Base64URL.from(plainRsaPriv["Exp"])
					)
				.firstCRTCoefficient(
						Base64URL.from(plainRsaPriv["Qi"])
					)
				.firstFactorCRTExponent(
						Base64URL.from(plainRsaPriv["Dp"])
					)
				.firstPrimeFactor(
						Base64URL.from(plainRsaPriv["P"])
					)
				.secondFactorCRTExponent(
						Base64URL.from(plainRsaPriv["Dq"])
					)
				.secondPrimeFactor(
						Base64URL.from(plainRsaPriv["Q"])
					)
				.build()
	}


	companion object {
		private const val serialVersionUID = "0.0.1"

		private fun decryptField(pw: String, data: String): String {
			return EncryptionService.decrypt(data, pw)
		}

		private fun getChecksumForRsaFields(plainRsaPub: Map<String, String?>, plainRsaPriv: Map<String, String?>): String {
			return EncryptionService.getSha256(
					plainRsaPub["Modulus"]!! +
					plainRsaPub["Exp"]!! +

					plainRsaPriv["Id"]!! +
					plainRsaPriv["Exp"]!! +
					plainRsaPriv["Qi"]!! +
					plainRsaPriv["Dp"]!! +
					plainRsaPriv["P"]!! +
					plainRsaPriv["Dq"]!! +
					plainRsaPriv["Q"]!!
				)
		}

		fun withId(pw: String, id: String): Builder {
			Assert.hasText(id, "id cannot be empty")
			return Builder(pw, id)
		}

		fun from(dbJwkRsaKey: DbJwkRsaKey): Builder {
			return Builder(dbJwkRsaKey)
		}

		fun from(pw: String, id: String, jwkRsaKey: RSAKey): Builder {
			return Builder(pw, id, jwkRsaKey)
		}

		class Builder : Serializable {
			companion object {
				private const val serialVersionUID = "0.0.1"
			}

			private var id: String? = null
			private var password: String? = null
			private var checksum: String? = null

			private val rsaPub: MutableMap<String, String?> = mutableMapOf()
			private val rsaPriv: MutableMap<String, String?> = mutableMapOf()

			constructor(pw: String, id: String) {
				this.id = id
				this.password = pw
			}

			constructor(dbJwkRsaKey: DbJwkRsaKey) {
				id = dbJwkRsaKey.id
				checksum = dbJwkRsaKey.checksum

				dbJwkRsaKey.plainRsaPub.forEach { (itKey, itVal) ->
						rsaPub[itKey] = itVal
					}
				dbJwkRsaKey.encrRsaPriv.forEach { (itKey, itVal) ->
						rsaPriv[itKey] = itVal
					}
			}

			constructor(pw: String, id: String, jwkRsaKey: RSAKey) {
				this.id = id
				this.password = pw

				this.rsaKeyPubModulus(jwkRsaKey.modulus)
				this.rsaKeyPubExp(jwkRsaKey.publicExponent)

				this.rsaKeyPrivId(jwkRsaKey.keyID)
				this.rsaKeyPrivExp(jwkRsaKey.privateExponent)
				this.rsaKeyPrivQi(jwkRsaKey.firstCRTCoefficient)
				this.rsaKeyPrivDp(jwkRsaKey.firstFactorCRTExponent)
				this.rsaKeyPrivP(jwkRsaKey.firstPrimeFactor)
				this.rsaKeyPrivDq(jwkRsaKey.secondFactorCRTExponent)
				this.rsaKeyPrivQ(jwkRsaKey.secondPrimeFactor)
			}

			@Suppress("WeakerAccess")
			fun rsaKeyPubModulus(rsaKeyPubModulo: Base64URL): Builder {
				this.rsaPub["Modulus"] = rsaKeyPubModulo.toString()
				return this
			}

			fun rsaKeyPubModulus(rsaKeyPubModulo: BigInteger): Builder {
				this.rsaPub["Modulus"] = Base64URL.encode(rsaKeyPubModulo).toString()
				return this
			}

			@Suppress("WeakerAccess")
			fun rsaKeyPubExp(rsaKeyPubExp: Base64URL): Builder {
				this.rsaPub["Exp"] = rsaKeyPubExp.toString()
				return this
			}

			fun rsaKeyPubExp(rsaKeyPubExp: BigInteger): Builder {
				this.rsaPub["Exp"] = Base64URL.encode(rsaKeyPubExp).toString()
				return this
			}

			@Suppress("WeakerAccess")
			fun rsaKeyPrivId(rsaKeyPrivId: String): Builder {
				this.rsaPriv["Id"] = rsaKeyPrivId
				return this
			}

			@Suppress("WeakerAccess")
			fun rsaKeyPrivExp(rsaKeyPrivExp: Base64URL): Builder {
				this.rsaPriv["Exp"] = rsaKeyPrivExp.toString()
				return this
			}

			fun rsaKeyPrivExp(rsaKeyPrivExp: BigInteger): Builder {
				this.rsaPriv["Exp"] = Base64URL.encode(rsaKeyPrivExp).toString()
				return this
			}

			@Suppress("WeakerAccess")
			fun rsaKeyPrivQi(rsaKeyPrivQi: Base64URL): Builder {
				this.rsaPriv["Qi"] = rsaKeyPrivQi.toString()
				return this
			}

			@Suppress("WeakerAccess")
			fun rsaKeyPrivDp(rsaKeyPrivDp: Base64URL): Builder {
				this.rsaPriv["Dp"] = rsaKeyPrivDp.toString()
				return this
			}

			@Suppress("WeakerAccess")
			fun rsaKeyPrivP(rsaKeyPrivP: Base64URL): Builder {
				this.rsaPriv["P"] = rsaKeyPrivP.toString()
				return this
			}

			@Suppress("WeakerAccess")
			fun rsaKeyPrivDq(rsaKeyPrivDq: Base64URL): Builder {
				this.rsaPriv["Dq"] = rsaKeyPrivDq.toString()
				return this
			}

			@Suppress("WeakerAccess")
			fun rsaKeyPrivQ(rsaKeyPrivQ: Base64URL): Builder {
				this.rsaPriv["Q"] = rsaKeyPrivQ.toString()
				return this
			}

			fun build(): DbJwkRsaKey {
				Assert.hasText(id, "id cannot be empty")
				if (password != null) {
					Assert.hasText(id, "password cannot be empty if password != null")
				}

				rsaPub.forEach { (itKey, itVal: String?) ->
						Assert.hasText(itVal, "RsaPub '$itKey' cannot be empty")
					}
				rsaPriv.forEach { (itKey, itVal: String?) ->
						Assert.hasText(itVal, "RsaPriv '$itKey' cannot be empty")
					}
				return create()
			}

			private fun create(): DbJwkRsaKey {
				val dbJwkRsaKey = DbJwkRsaKey()
				dbJwkRsaKey.id = id!!

				if (password == null) {
					Assert.notNull(checksum, "checksum cannot be null if password == null")
					dbJwkRsaKey.checksum = checksum!!
				} else {
					checksum = getChecksumForRsaFields(rsaPub, rsaPriv)
					dbJwkRsaKey.checksum = encryptField(checksum!!)
				}

				rsaPub.forEach { (itKey, itVal: String?) ->
						dbJwkRsaKey.plainRsaPub[itKey] = itVal!!
					}
				rsaPriv.forEach { (itKey, itVal: String?) ->
						dbJwkRsaKey.encrRsaPriv[itKey] = encryptField(itVal!!)
					}

				return dbJwkRsaKey
			}

			private fun encryptField(data: String): String {
				if (password == null) {
					return data
				}
				return EncryptionService.encrypt(data, password!!)
			}
		}
	}
}
