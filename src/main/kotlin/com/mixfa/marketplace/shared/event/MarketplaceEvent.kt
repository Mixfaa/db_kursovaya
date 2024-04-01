package com.mixfa.marketplace.shared.event

import org.springframework.context.ApplicationEvent

abstract class MarketplaceEvent(src: Any) : ApplicationEvent(src)

