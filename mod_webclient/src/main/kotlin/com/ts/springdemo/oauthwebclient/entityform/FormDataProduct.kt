package com.ts.springdemo.oauthwebclient.entityform

import javax.validation.constraints.NotNull
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Size


@Suppress("unused")
class FormDataProduct {

	@NotNull
	@Size(min=1, max=256)
	var desc: String? = null

	@NotNull
	@DecimalMin("0.01")
	var price: Double? = null
}
