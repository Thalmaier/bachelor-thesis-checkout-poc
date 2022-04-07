package secondary.repository.product

import core.domain.product.model.Product
import core.domain.product.model.ProductId
import secondary.repository.common.Cache

interface ProductCache : Cache<ProductId, Product>