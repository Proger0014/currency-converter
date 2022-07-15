package com.example.currencyconverter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.currencyconverter.databinding.ActivityMainBinding

/**
 * TODO:
 *  1. Сделать так, чтобы дубликаты с обеих сторон игнорились, ведь и там, и там могут быть два разных radioButton выбрано
 *  2. Сделать так, чтобы выбранный дубликат, если справа тоже, что и слева, то тот, который был последним выбранным, автоматом перешел на предыдущий radioButton
 */
class MainActivity : AppCompatActivity() {

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
                // проверяю, дубликаты выбраны
                var isDublicated = checkDublicateCheckedRadiobutton()

                var ignoreOption = getUncheckDublicateRadioButton()

                checkAllRadiobuttons(ignoreOption)
            }
        }

        // В зависимости от того, какая radiobutton в toOptions выбрана, валюта перед кол-во будет отображаться
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
                // проверяю, дубликаты выбраны
                var isDublicated = checkDublicateCheckedRadiobutton()


                var ignoreOption = getUncheckDublicateRadioButton()

                checkAllRadiobuttons(ignoreOption)

                displayAmount(0.0, optionId)
            }
        }
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

        // оставляет 2 числа после запятой у amount. Возвращает стркоу
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
     *  возращает дубликат option, который нужно будет игнорировать
     *
     * @return ignoreOption: String - тот самый дубликат option, который нужно игнорировать
     */
    private fun getUncheckDublicateRadioButton(): String {

        var ignoreOption = ""

        if (lastCheckedOptions == FROM_OPTIONS) {

            if (currentOptionFromOptions == R.id.currency_from_option_dollar) {
                ignoreOption = "to_dollar"
            } else if (currentOptionFromOptions == R.id.currency_from_option_euro) {
                ignoreOption = "to_euro"
            } else if (currentOptionFromOptions == R.id.currency_from_option_ruble) {
                ignoreOption = "to_ruble"
            } else if (currentOptionFromOptions == R.id.currency_from_option_yen) {
                ignoreOption = "to_yen"
            }
        } else if (lastCheckedOptions == TO_OPTIONS) {

            if (currentOptionToOptions == R.id.currency_to_option_dollar) {
                ignoreOption = "from_dollar"
            } else if (currentOptionToOptions == R.id.currency_to_option_euro) {
                ignoreOption = "from_euro"
            } else if (currentOptionToOptions == R.id.currency_to_option_ruble) {
                ignoreOption = "from_ruble"
            } else if (currentOptionToOptions == R.id.currency_to_option_yen) {
                ignoreOption = "from_yen"
            }
        }

        return ignoreOption
    }

    /**
     * summary:
     *  все кнопки делает активными, кроме одной кнопки, которая является дубликатом
     *
     * @param optionToIgnore - есть дубликат, который нужно игнорировать
     */
    private fun checkAllRadiobuttons(optionToIgnore: String) {
        binding.currencyToOptionDollar.isEnabled = true
        binding.currencyToOptionEuro.isEnabled = true
        binding.currencyToOptionRuble.isEnabled = true
        binding.currencyToOptionYen.isEnabled = true

        binding.currencyFromOptionDollar.isEnabled = true
        binding.currencyFromOptionEuro.isEnabled = true
        binding.currencyFromOptionRuble.isEnabled = true
        binding.currencyFromOptionYen.isEnabled = true

        when (optionToIgnore) {
            "to_dollar" -> binding.currencyToOptionDollar.isEnabled = false
            "to_euro" -> binding.currencyToOptionEuro.isEnabled = false
            "to_ruble" -> binding.currencyToOptionRuble.isEnabled = false
            "to_yen" -> binding.currencyToOptionYen.isEnabled = false

            "from_dollar" -> binding.currencyFromOptionDollar.isEnabled = false
            "from_euro" -> binding.currencyFromOptionEuro.isEnabled = false
            "from_ruble" -> binding.currencyFromOptionRuble.isEnabled = false
            "from_yen" -> binding.currencyFromOptionYen.isEnabled = false
        }
    }
}