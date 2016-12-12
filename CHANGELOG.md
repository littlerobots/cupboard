2.2.0
-----
* Order of `FieldConverterFactory` and `EntityConverterFactory` was not properly respected which would result in the wrong converter used when
registering multiple factories for the same type. (#39)
* `CupboardBuilder` copies converters (#35)
* `ReflectiveEntityConverter` exposes `mEntityClass`, useful for extending (#37)
* `Cupboard.isRegistered()` will now return `true` if either the entity or one of its super classes are registered. This allows for registering 
an (abstract) class or interface as a common table for all subclasses. Other than allowing the registration there's no support for polymorphism by default.
* When iterating a result set, the cursor `moveToNext` would be called in `next()`, which is not always desirable. This also means that `cupboard().withCursor(cursor).get()` won't
 move the cursor any more.
