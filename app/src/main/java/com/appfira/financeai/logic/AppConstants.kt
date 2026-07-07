package com.appfira.financeai.logic

object AppConstants {
    const val DRIVE_FOLDER_NAME = "Finance Tracker"
    const val DEFAULT_SPREADSHEET_TITLE = "Finance AI Tracker"
    const val SHARED_SPREADSHEET_TITLE = "Finance AI Shared"

    const val DATE_FORMAT_FULL = "dd/MM/yyyy HH:mm"
    const val DATE_FORMAT_TIME = "HH:mm"

    // Categories (Internal Keys) - Used for database and resource mapping
    const val CAT_FOOD = "category_food"
    const val CAT_BILL = "category_bill"
    const val CAT_TRANSPORT = "category_transport"
    const val CAT_SALARY = "category_salary"
    const val CAT_BONUS = "category_bonus"
    const val CAT_SHOPPING = "category_shopping"
    const val CAT_HEALTH = "category_health"
    const val CAT_EDUCATION = "category_education"
    const val CAT_INVESTMENT = "category_investment"
    const val CAT_ENTERTAINMENT = "category_entertainment"
    const val CAT_HOME = "category_home"
    const val CAT_TRAVEL = "category_travel"
    const val CAT_CLOTHING = "category_clothing"
    const val CAT_OTHER = "category_other"
    
    val CATEGORY_MAP = mapOf(
        // Indonesian
        "makan" to CAT_FOOD,
        "minum" to CAT_FOOD,
        "kuliner" to CAT_FOOD,
        "warung" to CAT_FOOD,
        "restoran" to CAT_FOOD,
        "listrik" to CAT_BILL,
        "pdam" to CAT_BILL,
        "internet" to CAT_BILL,   // satu entry (key unik)
        "wifi" to CAT_BILL,
        "pulsa" to CAT_BILL,
        "kuota" to CAT_BILL,
        "bensin" to CAT_TRANSPORT,
        "pertalite" to CAT_TRANSPORT,
        "pertamax" to CAT_TRANSPORT,
        "gojek" to CAT_TRANSPORT,
        "grab" to CAT_TRANSPORT,
        "ojek" to CAT_TRANSPORT,
        "parkir" to CAT_TRANSPORT,
        "bus" to CAT_TRANSPORT,    // satu entry (key unik)
        "kereta" to CAT_TRANSPORT,
        "gaji" to CAT_SALARY,
        "upah" to CAT_SALARY,
        "payroll" to CAT_SALARY,
        "bonus" to CAT_BONUS,      // satu entry (key unik)
        "komisi" to CAT_BONUS,
        "untung" to CAT_BONUS,
        "reward" to CAT_BONUS,     // satu entry (key unik)
        "belanja" to CAT_SHOPPING,
        "shopee" to CAT_SHOPPING,
        "tokopedia" to CAT_SHOPPING,
        "lazada" to CAT_SHOPPING,
        "alfamart" to CAT_SHOPPING,
        "indomaret" to CAT_SHOPPING,
        "dokter" to CAT_HEALTH,
        "obat" to CAT_HEALTH,
        "apotek" to CAT_HEALTH,
        "rs" to CAT_HEALTH,
        "rumah sakit" to CAT_HEALTH,
        "sekolah" to CAT_EDUCATION,
        "kursus" to CAT_EDUCATION,
        "buku" to CAT_EDUCATION,
        "saham" to CAT_INVESTMENT,
        "reksadana" to CAT_INVESTMENT,
        "crypto" to CAT_INVESTMENT, // satu entry (key unik)
        "nonton" to CAT_ENTERTAINMENT,
        "bioskop" to CAT_ENTERTAINMENT,
        "game" to CAT_ENTERTAINMENT, // satu entry (key unik)
        "streaming" to CAT_ENTERTAINMENT,
        "rumah" to CAT_HOME,
        "perabot" to CAT_HOME,
        "sewa" to CAT_HOME,
        "kontrakan" to CAT_HOME,
        "liburan" to CAT_TRAVEL,
        "hotel" to CAT_TRAVEL,
        "tiket" to CAT_TRAVEL,
        "pesawat" to CAT_TRAVEL,
        "baju" to CAT_CLOTHING,
        "celana" to CAT_CLOTHING,
        "pakaian" to CAT_CLOTHING,
        "sepatu" to CAT_CLOTHING,

        // English-only keywords (tidak ada duplikat dengan Indonesian)
        "food" to CAT_FOOD,
        "drink" to CAT_FOOD,
        "dinner" to CAT_FOOD,
        "lunch" to CAT_FOOD,
        "breakfast" to CAT_FOOD,
        "eat" to CAT_FOOD,
        "restaurant" to CAT_FOOD,
        "electricity" to CAT_BILL,
        "water" to CAT_BILL,
        "bill" to CAT_BILL,
        "subscription" to CAT_BILL,
        "gasoline" to CAT_TRANSPORT,
        "petrol" to CAT_TRANSPORT,
        "taxi" to CAT_TRANSPORT,
        "parking" to CAT_TRANSPORT,
        "train" to CAT_TRANSPORT,
        "salary" to CAT_SALARY,
        "wage" to CAT_SALARY,
        "shopping" to CAT_SHOPPING,
        "shop" to CAT_SHOPPING,
        "mall" to CAT_SHOPPING,
        "doctor" to CAT_HEALTH,
        "medicine" to CAT_HEALTH,
        "pharmacy" to CAT_HEALTH,
        "hospital" to CAT_HEALTH,
        "school" to CAT_EDUCATION,
        "course" to CAT_EDUCATION,
        "book" to CAT_EDUCATION,
        "stock" to CAT_INVESTMENT,
        "investment" to CAT_INVESTMENT,
        "movie" to CAT_ENTERTAINMENT,
        "cinema" to CAT_ENTERTAINMENT,
        "entertainment" to CAT_ENTERTAINMENT,
        "home" to CAT_HOME,
        "rent" to CAT_HOME,
        "travel" to CAT_TRAVEL,
        "holiday" to CAT_TRAVEL,
        "vacation" to CAT_TRAVEL,
        "flight" to CAT_TRAVEL,
        "clothing" to CAT_CLOTHING,
        "clothes" to CAT_CLOTHING,
        "shirt" to CAT_CLOTHING,
        "shoes" to CAT_CLOTHING
    )
    
    val EXPENSE_KEYWORDS = listOf(
        "beli", "bayar", "kurang", "keluar", "makan", "jajan", "nongkrong", "topup",
        "buy", "pay", "spend", "expense", "eat", "purchase"
    )
    val INCOME_KEYWORDS = listOf(
        "terima", "dapat", "gaji", "bonus", "masuk", "untung", "cair", "upah",
        "receive", "get", "income", "salary", "profit", "deposit"
    )
    
    // Default descriptions (Keys)
    const val DESC_INCOME_DEFAULT = "DEFAULT_INCOME"
    const val DESC_EXPENSE_DEFAULT = "DEFAULT_EXPENSE"
}
