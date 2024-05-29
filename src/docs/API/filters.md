# Custom Filter
Some methods (e.g. the `getSoundFiles` methods in the ServerApi) can be passed a filter argument to filter the results. By default there are two predefined filters. However, if you want to add your own filter, follow these steps:

1. Create a new class that either extends from [AbstractFilter](../javadoc/de/mrjulsen/dragnsounds/core/data/filter/AbstractFilter.html) or (if more controllers are needed) implements the [IFilter](../javadoc/de/mrjulsen/dragnsounds/core/data/filter/IFilter.html) interface.
2. Override the `isValid()` method and include an implementation of your filter.
3. Now register the filter once in the [FilterRegistry](../javadoc/de/mrjulsen/dragnsounds/registry/FilterRegistry.html) right at the beginning when loading the mod.