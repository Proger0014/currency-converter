package com.example.currencyconverter

/**
 * Описание:
 *  Хранит константы текста дубликатов выбора, который будет использоваться
 *  в сообщении об ошибке. Также содержит методы,
 *  который отдает нужный DublicateOption по имени
 */
class DublicateOption(private val name: String, fromOption: String, toOption: String) {

    val FROM_OPTION: String = fromOption
    val TO_OPTION: String = toOption

    companion object {
        /**
         * Описание:
         *  Возвращает DublicateOption по name
         *
         * @param name - имя DublicateOption, который нужно использовать
         *
         * @return DublicateOption? - может быть null, если отсутствует
         */
        fun List<DublicateOption>.getDublicateOptionByName(name: String): DublicateOption? {
            var result: DublicateOption? = null

            for (i in this) {
                if (i.name == name) {
                    result = i
                }
            }

            return result
        }
    }
}