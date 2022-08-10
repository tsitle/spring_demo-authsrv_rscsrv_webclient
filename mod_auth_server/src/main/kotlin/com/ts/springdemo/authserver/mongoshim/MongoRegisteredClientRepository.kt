package com.ts.springdemo.authserver.mongoshim

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.CriteriaDefinition
import org.springframework.data.mongodb.core.query.Query
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.config.ConfigurationSettingNames
import org.springframework.security.oauth2.server.authorization.config.TokenSettings
import org.springframework.util.Assert
import java.time.Duration


class MongoRegisteredClientRepository(
			private val mongoTemplate: MongoTemplate?
		) : RegisteredClientRepository {

	override fun save(registeredClient: RegisteredClient) {
		val existingClient = findByClientId(registeredClient.clientId)
		if (existingClient != null) {
			mongoTemplate?.remove(existingClient)
		}
		mongoTemplate?.save(registeredClient)
	}

	override fun findById(id: String): RegisteredClient? {
		Assert.hasText(id, "id cannot be empty")
		return findOneBy(Criteria.where("id").`is`(id))
	}

	override fun findByClientId(clientId: String): RegisteredClient? {
		Assert.hasText(clientId, "clientId cannot be empty")
		return findOneBy(Criteria.where("clientId").`is`(clientId))
	}

	// -----------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------

	private fun findBy(criteriaDef: CriteriaDefinition): List<RegisteredClient>? {
		val query = Query()
		query.addCriteria(criteriaDef)
		val res = mongoTemplate?.find(query, RegisteredClient::class.java)
		if (res == null || res.size == 0) {
			return res
		}
		// convert accessTokenTimeToLive and refreshTokenTimeToLive from String to Duration
		val moddedRes = mutableSetOf<RegisteredClient>()
		res.iterator().forEach { itRc ->
				val tmpNewRegClient = RegisteredClient.withId(itRc.id)
						.tokenSettings(
								TokenSettings.builder()
										.accessTokenTimeToLive(Duration.parse(itRc.tokenSettings.settings[ConfigurationSettingNames.Token.ACCESS_TOKEN_TIME_TO_LIVE] as String))
										.refreshTokenTimeToLive(Duration.parse(itRc.tokenSettings.settings[ConfigurationSettingNames.Token.REFRESH_TOKEN_TIME_TO_LIVE] as String))
										.build()
							)
						.clientId(itRc.clientId)
						.clientSecret(itRc.clientSecret)
				itRc.clientAuthenticationMethods.iterator().forEach { itCam ->
						tmpNewRegClient.clientAuthenticationMethod(itCam)
					}
				itRc.authorizationGrantTypes.iterator().forEach { itAgt ->
						tmpNewRegClient.authorizationGrantType(itAgt)
					}
				itRc.redirectUris.iterator().forEach { itRu ->
						tmpNewRegClient.redirectUri(itRu)
					}
				itRc.scopes.iterator().forEach { itSc ->
						tmpNewRegClient.scope(itSc)
					}
				moddedRes.add(tmpNewRegClient.build())
			}
		return moddedRes.toList()
	}

	private fun findOneBy(criteriaDef: CriteriaDefinition): RegisteredClient? {
		val res = findBy(criteriaDef)
		if (res == null || res.size != 1) {
			return null
		}
		return res[0]
	}
}
