package com.mixfa.marketplace.shared.model

import org.springframework.context.ApplicationEvent

abstract class MarketplaceEvent(src: Any) : ApplicationEvent(src)