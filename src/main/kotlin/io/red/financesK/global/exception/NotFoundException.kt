package io.red.financesK.global.exception

class NotFoundException (message: String) : RuntimeException(message) {
    constructor(entityName: String, id: Int) : this("$entityName with ID $id not found")
    constructor(entityName: String, id: String) : this("$entityName with ID $id not found")
}
