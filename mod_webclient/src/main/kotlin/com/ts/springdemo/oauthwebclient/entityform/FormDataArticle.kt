package com.ts.springdemo.oauthwebclient.entityform

import javax.validation.constraints.NotNull
import javax.validation.constraints.Size


@Suppress("unused")
class FormDataArticle {

	@NotNull
	@Size(min=1)
	var linesStr: String? = null
}
