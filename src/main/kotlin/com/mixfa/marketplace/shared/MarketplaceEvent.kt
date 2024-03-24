package com.mixfa.marketplace.shared

import org.springframework.context.ApplicationEvent

abstract class MarketplaceEvent(src: Any) : ApplicationEvent(src)

