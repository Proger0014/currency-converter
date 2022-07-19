package com.example.currencyconverter

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RadioButton
import com.example.currencyconverter.databinding.ActivityMainBinding

/**
 * note:
 *  ? - проверить, работает ли
 *
 *  реализовать конвертер валют с обменными курсами каждой валюты к каждой:
 *      1 dollar = 56,56r
 *      114 yen = 1 dollar (без главной валюты)
 *
 * TODO:
 *  UI
 *  1. [x] при выборе option, цвет текста дубликата только меняет цвет на серый, но также активный
 *      1.2 [x] настроить адаптивные цвета для светлой и темной тем
 *       - светлая тема:
 *        обычный: черный текст
 *        дубликат: gray
 *       - темная тема:
 *        обычный: белый текст
 *        дубликат: фиолетовый (colorPrimaryVariant)
 *      1.3 [ ] цвет дубликата в темной теме сделать чуть посветлее
 *  2. [x] элементы от options опустить на +-10dp для сообщения об ошибки для пункта 3
 *  3. [ ] дубликаты при запуске приложения должны быть подсвечены с самого начала
 *  4. [ ] добавить textView, на котором будут отображаться сообщения об ошибке (может быть, и другие сообщения?)
 *  5. [ ] если выбраны дубликаты, то при кнопке "перевести" дубликаты подсвечиваются красным и пишется снизу, от options,
 *         красным цветом ошибка (просто смена цвета текста и его добавление)
 *  6. [ ] при клике "перевести", клавиатура должна скрываться (подсказка в wiki)
 *  BusinessLogic
 *  7. [ ] создать классы для валют, в которых будет храниться курс валюты к главной валюте
 *      7.1 [ ] создать главную валюту, где будут храниться курс этой валюты к каждой хранимой валюте
 *  8. [ ] реализовать логику конвертации из одной валюты в другую
 *      - если валюта "из" != dollar, то кол-во валюты "из" умножается на курс валюты к доллару:
 *        34 йены * 0,0072453 (курс йены к доллару)
 *      - если валюта "из" == dollar, то кол-во долларов "из" умножается на курс доллара к валюте:
 *        34 dollars * 138,02 yen (курс доллара к йен)
 *
 *
 * TODO:
 *  РЕФАКТОРИНГ
 *  1. [ ] Заменить тип свойства, хранящие текущий и предыдущий id option на RadioButton!!!!
 *  2. [ ] Подумать над тем, как отрефакторить displayAmount и подобные ему
 *         Чтобы отображаемый символ не в методе хранился, а в классе.
 *         Или выводимый "to_dollar" и прочее хранится в классе (вынести сущности в отдельные классы)
 *  ЗАПИСЬ В WIKI
 *  1. [ ] Записать про SupressLint и attrs/themes и использование кода, чтобы получить кастомный цвет из themes
 */
class MainActivity : AppCompatActivity() {

    /**
     * Константы для lastCheckedOptions, чтобы хранить, что последним было выбрано
     * Будет использоваться для того, чтобы возвращать disabled кнопку в предыдущее положение,
     * но с противоположной стороны, где был выбран option.
     *
     * Пример:
     *  в toOptions выбрана 2 radioButton по-умолчанию
     *
     *  [Возможно, в этом нет необходимости, в этой функциональности]
     */
    private val FROM_OPTIONS = "currencyFromOptions"
    private val TO_OPTIONS = "currencyToOptions"

    private var lastCheckedOptions: String = ""

    /**
     * Свойства, в которых хранятся текущий выбранный checkedButton у currencyFromOptions
     * и предыдущий выбранный айдишник
     */
    private var currentOptionFromOptions: Int = -1
    private var previousOptionFromOptions: Int = -1

    /**
     * хранит дефолтный цвет текста для options
     */
    private var lastOptionTextColor: Int = -1

    /**
     * Свойства, в которых хранятся текущий выбранный checkedButton у currencyToOptions
     * и предыдущий выбранный айдишник
     */
    private var currentOptionToOptions: Int = -1
    private var previousOptionToOptions: Int = -1


    /**
     * приватная переменная, в которой будет обьект привязки для activity_main.xml
     */
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // инициализация обьекта привязки
        binding = ActivityMainBinding.inflate(layoutInflater)
        // для main activity cтавится layout от корня обьетка привязки, со всеми ветвлениями
        setContentView(binding.root)

        // при запуске сразу отображает "Количество: $0.0"
        displayAmount(0.0, binding.currencyToOptions.checkedRadioButtonId)

        // инициализация полей выбранных options
        currentOptionToOptions = binding.currencyToOptions.checkedRadioButtonId
        currentOptionFromOptions = binding.currencyFromOptions.checkedRadioButtonId

        // обработчик смены checkedRadioButton внутри currencyFromOptions
        binding.currencyFromOptions.setOnCheckedChangeListener { radioGroup, optionId ->
            run {
                // ставлю, что был изменен fromOptions последним
                lastCheckedOptions = FROM_OPTIONS

                /**
                 * Описание:
                 *  Сохраняется текущий и предыдущий состояния FromOptions
                 */
                if (currentOptionFromOptions == -1) {
                    currentOptionFromOptions = optionId
                } else {
                    previousOptionFromOptions = currentOptionFromOptions
                    currentOptionFromOptions = optionId
                }

                // получение option, который должен быть подсвечен серым (с противоположной стороны)
                val ignoreOption = getUncheckDublicateFromOptions()

                // сделать ignoreOption с серым текстом
                checkToOptions(ignoreOption)
            }
        }

        // обработка выбора toOptions
        binding.currencyToOptions.setOnCheckedChangeListener { radioGroup, optionId ->
            run {
                // ставлю, что был изменен toOptions последним
                lastCheckedOptions = TO_OPTIONS

                /**
                 * Описание:
                 *  Сохраняется текущий и предыдущий состояния ToOptions
                 */
                if (currentOptionToOptions == -1) {
                    currentOptionToOptions = optionId
                } else {
                    previousOptionToOptions = currentOptionToOptions
                    currentOptionToOptions = optionId
                }

                // изменяет знак валюты на выбранный toOptions с кол-во 0.0
                displayAmount(0.0, currentOptionToOptions)

                // получение option, который нужно игнорировать (с противоположной стороны)
                val ignoreOption = getUncheckDublicateToOptions()

                // игнорирование этого option
                checkFromOptions(ignoreOption)
            }
        }
    }

    /**
     * @param option - тот самый radioButton, цвет текст которого поменяется на color
     * @param color - цвет, на который поменяет цвет текста option
     *
     * меняет цвет текста option на color
     */
    @SuppressLint("ResourceAsColor")
    private fun changeOptionTextColor(option: RadioButton, color: Int) {
        option.setTextColor(color)
    }

    /**
     * @param option - тот самый radioButton, цвет текста которого поменяется на красный
     *
     * меняет цвет текста на красный - цвет ошибки
     */
    @SuppressLint("ResourceAsColor")
    private fun changeOptionTextColorError(option: RadioButton) {

        val colorRed = getColorTheme(3)

        option.setTextColor(colorRed)
    }

    /**
     * @param amount - деньги, которые будут отображены в результате кол-во денег
     * @param toConvertCurrency - id-шник radioButton, которая была чекнута
     *
     * summary:
     *  в зависимости от toConvertCurrency и будет зависеть, что будет отображаться перед amount, какой символ валюты
     */
    private fun displayAmount(amount: Double, toConvertCurrency: Int) {

        val currencySymbol = when (toConvertCurrency) {
            binding.currencyToOptionYen.id -> "¥"
            binding.currencyToOptionEuro.id -> "€"
            binding.currencyToOptionDollar.id -> "$"
            else -> "₽"
        }

        // оставляет 2 числа после запятой у amount. Возвращает строку
        val formattedAmount = "${currencySymbol}%.2f".format(amount)
        binding.currencyResult.text = getString(R.string.currency_result_text, formattedAmount)
    }

    /**
     * summary:
     *  проверяет, выбраны ли одинаковые radioButtons с разных options
     *
     * returns:
     *  возвращает true, если выбраны одинаковые
     *  возвращает false, если выбраны разные
     */
    private fun checkDublicateCheckedRadiobutton(): Boolean {

        var isDublicate = false

        if (currentOptionFromOptions == R.id.currency_from_option_dollar && currentOptionToOptions == R.id.currency_to_option_dollar) {
            isDublicate = true
        } else if (currentOptionFromOptions == R.id.currency_from_option_euro && currentOptionToOptions == R.id.currency_to_option_euro) {
            isDublicate = true
        } else if (currentOptionFromOptions == R.id.currency_from_option_ruble && currentOptionToOptions == R.id.currency_to_option_ruble) {
            isDublicate = true
        } else if (currentOptionFromOptions == R.id.currency_from_option_yen && currentOptionToOptions == R.id.currency_to_option_yen) {
            isDublicate = true
        }


        return isDublicate
    }

    /**
     * summary:
     *  возращает дубликат option из toOptions, который нужно будет игнорировать
     *
     * @return ignoreOption: String - тот самый дубликат option для toOptions, который нужно игнорировать
     */
    private fun getUncheckDublicateToOptions(): String {

        var ignoreOption = ""

        when (currentOptionToOptions) {
            R.id.currency_to_option_dollar -> {
                ignoreOption = "from_dollar"
            }
            R.id.currency_to_option_euro -> {
                ignoreOption = "from_euro"
            }
            R.id.currency_to_option_ruble -> {
                ignoreOption = "from_ruble"
            }
            R.id.currency_to_option_yen -> {
                ignoreOption = "from_yen"
            }
        }

        return ignoreOption
    }

    /**
     * summary:
     *  возращает дубликат option из fromOptions, который нужно будет игнорировать
     *
     * @return ignoreOption: String - тот самый дубликат option для fromOptions, который нужно игнорировать
     */
    private fun getUncheckDublicateFromOptions(): String {

        var ignoreOption = ""

        when (currentOptionFromOptions) {

            R.id.currency_from_option_dollar -> {
                ignoreOption = "to_dollar"
            }
            R.id.currency_from_option_euro -> {
                ignoreOption = "to_euro"
            }
            R.id.currency_from_option_ruble -> {
                ignoreOption = "to_ruble"
            }
            R.id.currency_from_option_yen -> {
                ignoreOption = "to_yen"
            }
        }

        return ignoreOption
    }

    /**
     * summary:
     *  все кнопки toOptions делает активными, кроме одной кнопки, которая является дубликатом
     *
     * @param optionToIgnore - есть дубликат, который нужно игнорировать
     */
    private fun checkToOptions(optionToIgnore: String) {

        val colorOnSurface = getColorTheme(1)
        val colorDublicate = getColorTheme(2)

        changeOptionTextColor(binding.currencyToOptionDollar, colorOnSurface)
        changeOptionTextColor(binding.currencyToOptionEuro, colorOnSurface)
        changeOptionTextColor(binding.currencyToOptionRuble, colorOnSurface)
        changeOptionTextColor(binding.currencyToOptionYen, colorOnSurface)

        when (optionToIgnore) {
            "to_dollar" -> changeOptionTextColor(binding.currencyToOptionDollar, colorDublicate)
            "to_euro" -> changeOptionTextColor(binding.currencyToOptionEuro, colorDublicate)
            "to_ruble" -> changeOptionTextColor(binding.currencyToOptionRuble, colorDublicate)
            "to_yen" -> changeOptionTextColor(binding.currencyToOptionYen, colorDublicate)
        }
    }

    /**
     * TODO:
     *  [ ] Попробовать сократить на рефакторинге
     *
     * summary:
     *  все кнопки fromOptions делает активными, кроме одной кнопки, которая является дубликатом
     *
     * @param optionToIgnore - есть дубликат, который нужно игнорировать
     */
    private fun checkFromOptions(optionToIgnore: String) {

        val colorOnSurface = getColorTheme(1)
        val colorDublicate = getColorTheme(2)

        changeOptionTextColor(binding.currencyFromOptionDollar, colorOnSurface)
        changeOptionTextColor(binding.currencyFromOptionEuro, colorOnSurface)
        changeOptionTextColor(binding.currencyFromOptionRuble, colorOnSurface)
        changeOptionTextColor(binding.currencyFromOptionYen, colorOnSurface)

        when (optionToIgnore) {
            "from_dollar" -> changeOptionTextColor(binding.currencyFromOptionDollar, colorDublicate)
            "from_euro" -> changeOptionTextColor(binding.currencyFromOptionEuro, colorDublicate)
            "from_ruble" -> changeOptionTextColor(binding.currencyFromOptionRuble, colorDublicate)
            "from_yen" -> changeOptionTextColor(binding.currencyFromOptionYen, colorDublicate)
        }
    }

    /**
     * summary:
     *  Возвращает указанный цвет текста текущей темы
     *
     *  @param colorType - какой цвет я должен получить. 1 -> colorOnSurface, 2 -> colorDublicate, 3 -> colorError
     *
     *  @return color - цвет в виде resource id
     */
    @SuppressLint("ResourceAsColor")
    private fun getColorTheme(colorType: Int): Int {

        var color: Int = -1

        // получение цвета темы colorOnSurface
        val typedArray = theme.obtainStyledAttributes(R.styleable.ViewStyle)


        when (colorType) {
            1 -> {
                color = typedArray.getColor(R.styleable.ViewStyle_colorOnSurface, Color.BLACK)
            }
            2 -> {
                color = typedArray.getColor(R.styleable.ViewStyle_colorDublicate, R.color.gray)
            }
            3 -> {
                color = typedArray.getColor(R.styleable.ViewStyle_colorError, Color.RED)
            }
        }

        return color
    }
}