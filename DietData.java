package medbuddy;

import java.util.*;

public class DietData {

    public static class DietPlan {
        public String disease, tagline, icon;
        public String[] rawFoods;       // Raw / unprocessed things to include
        public String[] vegMeals;       // Full vegetarian meal options
        public String[] nonVegMeals;    // Full non-veg meal options
        public String[] avoid;
        public String[] breakfast;
        public String[] lunch;
        public String[] dinner;
        public String[] snacks;
        public String[] tips;
        // Conflict foods: things that are FINE for this disease but bad for others
        public String[] conflictWarnings;

        public DietPlan(String disease, String tagline, String icon,
                        String[] rawFoods, String[] vegMeals, String[] nonVegMeals,
                        String[] avoid, String[] breakfast, String[] lunch,
                        String[] dinner, String[] snacks, String[] tips,
                        String[] conflictWarnings) {
            this.disease = disease; this.tagline = tagline; this.icon = icon;
            this.rawFoods = rawFoods; this.vegMeals = vegMeals; this.nonVegMeals = nonVegMeals;
            this.avoid = avoid; this.breakfast = breakfast; this.lunch = lunch;
            this.dinner = dinner; this.snacks = snacks; this.tips = tips;
            this.conflictWarnings = conflictWarnings;
        }
    }

    private static final Map<String, DietPlan> plans = new LinkedHashMap<>();

    static {
        plans.put("Diabetes", new DietPlan(
            "Diabetes", "Control blood sugar with every meal", "🩸",
            // Raw foods
            new String[]{"Raw cucumber slices", "Raw carrot sticks", "Raw methi (fenugreek) leaves",
                "Jamun (Indian blackberry) — raw", "Amla (Indian gooseberry) — raw",
                "Soaked overnight almonds (5–6)", "Raw bitter gourd (karela) juice — 30ml morning",
                "Raw flaxseeds (1 tbsp, crush before eating)", "Raw chia seeds soaked in water",
                "Raw tomatoes", "Raw onion slices with meals"},
            // Veg meals
            new String[]{"Moong dal khichdi + cucumber raita + salad",
                "Brown rice + palak dal + karela sabzi",
                "Multigrain roti (2) + chana masala + lauki sabzi",
                "Oats upma with mixed vegetables + green chutney",
                "Rajma (kidney bean) bowl + brown rice + salad",
                "Tofu stir-fry with bell peppers + roti",
                "Methi thepla + low-fat curd"},
            // Non-veg meals
            new String[]{"Grilled chicken breast + steamed vegetables + brown rice",
                "Baked fish (rohu/salmon) + roti + salad",
                "Egg white omelette (2 eggs) + whole wheat toast + vegetables",
                "Chicken soup + multigrain roti",
                "Boiled egg (2) + sprout salad + roti",
                "Tuna salad with cucumber + whole wheat bread"},
            // Avoid
            new String[]{"White rice & white bread (maida)", "Sugary drinks & packaged juices",
                "Deep fried snacks (samosa, puri, pakora)", "Full-fat dairy",
                "Bananas, mangoes, grapes (high sugar fruits)", "Processed breakfast cereals",
                "Alcohol & smoking"},
            // Breakfast
            new String[]{"Oats porridge + soaked almonds + green tea",
                "Besan chilla (2) + green chutney + herbal tea",
                "Whole wheat toast + peanut butter (1 tsp) + boiled egg"},
            // Lunch
            new String[]{"Brown rice + moong dal + lauki sabzi + cucumber salad",
                "Multigrain roti (2) + rajma + onion-tomato salad",
                "Oats khichdi + low-fat curd + salad"},
            // Dinner
            new String[]{"Grilled paneer/chicken + stir-fried vegetables",
                "Moong dal soup + 1 roti + salad",
                "Vegetable daliya + low-fat curd"},
            // Snacks
            new String[]{"Roasted chana (handful)", "Apple + 5 almonds",
                "Cucumber + hummus", "Karela/amla juice"},
            // Tips
            new String[]{"Eat every 3–4 hours — never skip meals",
                "Walk 30 min after lunch & dinner",
                "Check blood sugar daily (fasting + post-meal)",
                "Drink 8–10 glasses of water daily",
                "Reduce stress — cortisol raises blood sugar"},
            // Conflict warnings
            new String[]{"Banana (avoid — ok for Vit D3)", "Full-fat dairy (avoid — ok for Thyroid)"}
        ));

        plans.put("PCOS", new DietPlan(
            "PCOS", "Balance hormones through mindful eating", "🌸",
            new String[]{"Raw spearmint leaves (brew as tea or chew)", "Raw flaxseeds (1 tbsp daily)",
                "Raw pumpkin seeds (1 handful)", "Raw cucumber + lemon juice",
                "Raw cinnamon powder on food (¼ tsp/day)", "Raw walnuts (4–5/day)",
                "Raw ginger slices in warm water", "Raw amla juice (morning)",
                "Raw sprouts (moong, chana, methi)", "Soaked fenugreek seeds in water (morning)"},
            new String[]{"Quinoa salad with chickpeas + olive oil dressing",
                "Palak paneer (low oil) + multigrain roti",
                "Oats bowl with berries + flaxseeds",
                "Rajma brown rice bowl + cucumber raita",
                "Tofu scramble with spinach + whole wheat roti",
                "Vegetable soup + besan roti",
                "Daliya khichdi + low-fat curd"},
            new String[]{"Grilled salmon + quinoa + steamed broccoli",
                "Egg omelette with spinach + whole wheat toast",
                "Baked chicken + sweet potato + green salad",
                "Tuna salad wrap (whole wheat) + fruit",
                "Boiled eggs (2) + avocado + roti"},
            new String[]{"Refined carbs (maida, white rice)", "Dairy (test individual tolerance)",
                "Processed/packaged foods", "Excess caffeine & alcohol",
                "Soy in excess (can affect estrogen)", "Trans fats (vanaspati, bakery items)"},
            new String[]{"Smoothie: spinach + flaxseed + berries + almond milk",
                "Oats with cinnamon + chia seeds + walnuts",
                "Besan chilla + green chutney + spearmint tea"},
            new String[]{"Quinoa + chickpea salad + olive oil dressing",
                "Brown rice + palak sabzi + raita",
                "Roti (2) + moong dal + stir-fried veggies"},
            new String[]{"Grilled fish/chicken + roasted vegetables",
                "Lentil soup + roti + salad",
                "Stir-fried tofu/paneer + brown rice"},
            new String[]{"Walnuts + 1 square dark chocolate", "Spearmint tea + roasted seeds",
                "Greek yogurt + berries", "Pumpkin seeds handful"},
            new String[]{"Manage stress — cortisol worsens PCOS hormones",
                "Exercise: yoga + cardio 5 days/week",
                "Sleep exactly 7–8 hours — sleep affects hormones critically",
                "Track your cycle and symptom patterns",
                "Consult endocrinologist + gynaecologist together"},
            new String[]{"Soy products (avoid excess — ok for Diabetes)", "Full-fat dairy (test — ok for Thyroid)"}
        ));

        plans.put("Thyroid", new DietPlan(
            "Thyroid", "Nourish your thyroid with the right nutrients", "🦋",
            new String[]{"Raw Brazil nuts (2/day — selenium)", "Raw pumpkin seeds (selenium + zinc)",
                "Raw sunflower seeds (1 tbsp)", "Soaked almonds (5–6)",
                "Raw apple + skin", "Cooked (not raw) broccoli, cauliflower",
                "Raw ginger in warm water", "Iodized salt in meals",
                "Cooked mushrooms (Vit D source)", "Raw coconut (for hypothyroid)"},
            new String[]{"Paneer + vegetable curry + brown rice",
                "Daliya + cooked greens + raita",
                "Rajma + roti + cooked methi sabzi",
                "Vegetable khichdi + curd (iodized salt)",
                "Tofu + stir-fried cooked vegetables + roti",
                "Moong dal soup + multigrain roti"},
            new String[]{"Scrambled eggs + whole wheat toast + glass of milk",
                "Baked fish + sweet potato + cooked vegetables",
                "Chicken curry (light) + brown rice + salad",
                "Boiled eggs (2) + oats porridge + milk",
                "Tuna + whole wheat sandwich + fruit"},
            new String[]{"Raw goitrogenic veggies: broccoli, cabbage, soy (cook them!)",
                "Gluten (some patients benefit reducing it)", "Excess caffeine + alcohol",
                "Processed & packaged foods", "Fluoride excess (filtered water preferred)",
                "Taking medication with calcium-rich foods"},
            new String[]{"Eggs (2) + whole wheat toast + glass of iodized milk",
                "Oats + banana + Brazil nuts (2) + warm water with ginger",
                "Upma + coconut chutney + herbal tea"},
            new String[]{"Brown rice + fish curry + cooked vegetables",
                "Roti + dal + cooked palak + curd",
                "Paneer + quinoa + cooked mixed veg"},
            new String[]{"Baked fish + sweet potato + steamed beans",
                "Egg curry + roti + salad",
                "Chicken/paneer stew + cooked vegs"},
            new String[]{"Pumpkin seeds + sunflower seeds mix",
                "Boiled egg", "Fruit (apple/pear)", "Curd with iodized salt"},
            new String[]{"Take thyroid medication on empty stomach — wait 1 hour before eating",
                "Avoid calcium/iron within 4 hours of medication",
                "Cook all goitrogenic vegetables (broccoli, cabbage)",
                "Recheck thyroid levels every 3–6 months",
                "Exercise improves thyroid metabolism"},
            new String[]{"Raw brassica veggies (broccoli, cabbage) — must cook for thyroid",
                "Soy (avoid — affects thyroid hormone absorption)"}
        ));

        plans.put("Heart Problems", new DietPlan(
            "Heart Problems", "Eat your way to a stronger heart", "❤️",
            new String[]{"Raw walnuts (4–5/day — omega-3)", "Raw flaxseeds (1 tbsp — crush them)",
                "Raw garlic clove on empty stomach (1 clove/day)", "Raw tomatoes",
                "Raw onion with meals", "Fresh berries", "Raw chia seeds soaked in water",
                "Raw spinach in salads", "Soaked almonds (5–6)", "Fresh lemon juice in water"},
            new String[]{"Brown rice + dal + sabzi (mustard oil/olive oil) + salad",
                "Oats porridge + banana + flaxseeds",
                "Moong dal khichdi + stir-fried greens",
                "Rajma + roti + cucumber salad",
                "Palak paneer (low oil) + multigrain roti",
                "Vegetable soup + multigrain toast"},
            new String[]{"Grilled/baked salmon + quinoa + mixed greens",
                "Baked chicken breast + roasted vegetables + salad",
                "Boiled egg whites + whole wheat toast + tomatoes",
                "Tuna salad (olive oil) + whole wheat bread",
                "Grilled fish + sweet potato + steamed broccoli"},
            new String[]{"Saturated fats: red meat, butter, full-fat dairy",
                "Trans fats: vanaspati, packaged biscuits & cakes",
                "High-sodium foods: pickles, papad, namkeen, sauces",
                "Refined carbohydrates & sugar", "Alcohol & smoking",
                "Coconut oil in excess"},
            new String[]{"Oats + banana + flaxseeds + green tea",
                "Whole wheat toast + avocado + boiled egg whites",
                "Poha (minimal oil) + green chutney + herbal tea"},
            new String[]{"Brown rice + dal (low oil) + sabzi + salad",
                "Multigrain roti + rajma + cucumber raita",
                "Grilled fish + quinoa + steamed vegetables"},
            new String[]{"Baked chicken/fish + roasted vegetables + soup",
                "Moong dal soup + 1 roti + salad",
                "Vegetable stew + small brown rice"},
            new String[]{"Walnuts (4–5)", "Apple slices", "Roasted chana (unsalted)", "Flaxseed water"},
            new String[]{"Keep sodium below 1500 mg/day — read all labels",
                "Exercise: 30–45 min cardio (walking, cycling) daily",
                "Monitor BP and cholesterol every 3 months",
                "Quit smoking — it triples heart disease risk",
                "Manage stress: deep breathing + meditation 10 min/day"},
            new String[]{"Coconut oil (avoid — ok for Thyroid hypothyroid)", "Red meat (avoid strictly)"}
        ));

        plans.put("Alzheimer's", new DietPlan(
            "Alzheimer's", "Feed your brain, protect your memory", "🧠",
            new String[]{"Fresh blueberries / mixed berries (daily)", "Raw walnuts (best brain nut)",
                "Raw flaxseeds + chia seeds", "Soaked almonds (5–6/day)",
                "Raw dark leafy greens in salads (spinach, kale)",
                "Turmeric + black pepper in food or warm milk",
                "Extra virgin olive oil (use raw as dressing)",
                "Raw broccoli sprouts", "Fresh pomegranate seeds", "Soaked overnight figs"},
            new String[]{"Spinach-walnut salad + olive oil dressing + whole grain bread",
                "Berry smoothie + oats + flaxseeds",
                "Brown rice + rajma + cooked greens",
                "Moong dal + vegetables + turmeric roti",
                "Quinoa bowl + chickpeas + mixed vegetables",
                "Daliya + cooked palak + curd"},
            new String[]{"Grilled salmon + mixed greens + olive oil",
                "Baked chicken + quinoa + roasted broccoli",
                "Egg omelette (with yolk — choline for brain) + toast",
                "Tuna wrap + spinach + whole wheat tortilla",
                "Sardines on whole grain toast + salad"},
            new String[]{"Red meat (limit to 2x/week)", "Butter & margarine",
                "Cheese in excess", "Pastries, cakes & sweets",
                "Fried & fast food", "High-sodium packaged foods",
                "Alcohol (accelerates brain decline)"},
            new String[]{"Oats + blueberries + walnuts + turmeric milk",
                "Whole grain cereal + mixed berries + almond milk",
                "Egg omelette (spinach + turmeric) + whole wheat toast"},
            new String[]{"Grilled salmon + quinoa + salad (olive oil)",
                "Brown rice + dal + palak sabzi",
                "Whole wheat roti + rajma + cucumber"},
            new String[]{"Baked chicken + roasted broccoli + sweet potato",
                "Lentil soup + roti + salad",
                "Vegetable stew + small brown rice"},
            new String[]{"Mixed berries (fresh)", "Walnuts + dark chocolate (1 piece)",
                "Turmeric golden milk", "Pomegranate seeds"},
            new String[]{"Stay socially + mentally active — talk, laugh, engage",
                "Exercise: 30 min daily improves brain blood flow",
                "Sleep 7–8 hours — brain detoxes waste during sleep",
                "Learn new things: music, language, puzzles daily",
                "Omega-3 supplements (fish oil) — discuss with doctor"},
            new String[]{"Alcohol (completely avoid)", "Fried foods (strictly avoid)"}
        ));

        plans.put("Vitamin D3 Deficiency", new DietPlan(
            "Vitamin D3 Deficiency", "Soak up sunshine & eat right", "☀️",
            new String[]{"Mushrooms (kept in sunlight 15–20 min before eating)",
                "Raw egg yolk (if comfortable)", "Raw fortified milk",
                "Raw cheese (in moderation)", "Soaked almonds",
                "Sun-dried tomatoes", "Orange / kinnow (fortified juice)",
                "Raw sesame seeds (calcium + D co-absorption)"},
            new String[]{"Paneer + vegetable curry + brown rice (fortified milk)",
                "Daliya + mushroom sabzi + glass of milk",
                "Rajma + roti + curd (full-fat for D absorption)",
                "Palak paneer + roti + fortified milk",
                "Vegetable khichdi + curd + sunflower seeds"},
            new String[]{"Grilled salmon + sweet potato + salad",
                "Sardine curry + roti + glass of milk",
                "Egg omelette (with yolk) + toast + OJ",
                "Baked mackerel + brown rice + vegetables",
                "Tuna + pasta (whole wheat) + cheese"},
            new String[]{"Very low-fat diet (D3 needs fat to absorb!)",
                "Excess alcohol (blocks Vit D activation)",
                "High phytate foods in excess (raw bran)", "Excess caffeine"},
            new String[]{"Eggs (2, with yolk) + fortified milk + toast",
                "Oats + fortified milk + banana",
                "Mushroom omelette + orange juice (fortified)"},
            new String[]{"Grilled salmon/mackerel + brown rice + vegetables",
                "Sardine curry + roti + salad",
                "Tuna sandwich + milk"},
            new String[]{"Baked fish + roasted mushrooms + sweet potato",
                "Egg curry + roti", "Paneer + vegetables + roti"},
            new String[]{"Cheese cubes (2–3)", "Boiled egg", "Fortified yogurt", "Sunflower seeds"},
            new String[]{"Get 15–20 min of morning sunlight (before 10 AM) on arms/legs",
                "Vitamin D3 supplement (1000–2000 IU/day) as prescribed",
                "Pair with Vitamin K2 for better bone absorption",
                "Pair with magnesium — needed for D3 activation",
                "Recheck blood levels after 3 months of supplementation"},
            new String[]{"Very low-fat diet (D3 needs dietary fat to absorb)"}
        ));

        plans.put("Vitamin B12 Deficiency", new DietPlan(
            "Vitamin B12 Deficiency", "Energize nerves & blood with B12-rich foods", "💊",
            new String[]{"Raw milk or warm milk (most absorbable form)",
                "Raw egg yolk (if comfortable)", "Fresh curd / yogurt",
                "Fresh paneer (cottage cheese)", "Raw nutritional yeast (for vegans)",
                "Soaked fenugreek seeds", "Fresh fermented foods (idli batter, dosa batter)",
                "Cheese (any variety)"},
            new String[]{"Paneer bhurji + roti + glass of milk",
                "Curd rice + papad (baked) + pickle (small)",
                "Idli (3–4) + sambar + curd",
                "Dosa + sambar + coconut chutney + milk",
                "Vegetable khichdi + curd + glass of milk",
                "Daliya + curd + cheese slice"},
            new String[]{"Eggs (2–3) + toast + glass of milk",
                "Chicken curry + brown rice + curd",
                "Fish + roti + dal + curd",
                "Egg fried rice (brown) + vegetables",
                "Grilled chicken + quinoa + salad",
                "Mutton (small portion) + roti + vegetable"},
            new String[]{"Alcohol (severely depletes B12)", "Excess antacids/PPIs (blocks absorption)",
                "Very high fibre diets in excess", "Processed foods",
                "Vegan diet without supplementation"},
            new String[]{"Eggs (2) + fortified milk + whole wheat toast",
                "Curd with fortified cereal + fruit",
                "Paneer paratha + glass of milk"},
            new String[]{"Chicken curry + brown rice + salad",
                "Fish + roti + dal + curd",
                "Egg fried rice + vegetables"},
            new String[]{"Grilled chicken/fish + roasted vegetables",
                "Paneer bhurji + roti + salad",
                "Tuna pasta (whole wheat) + salad"},
            new String[]{"Cheese slice on cracker", "Boiled egg", "Greek yogurt", "Glass of warm milk"},
            new String[]{"B12 supplements or injections as prescribed by doctor",
                "Vegans MUST supplement — zero real B12 in plant foods",
                "Absorption decreases with age — elderly need higher doses",
                "B12 deficiency causes irreversible nerve damage if ignored",
                "Check for pernicious anaemia (autoimmune B12 issue)"},
            new String[]{"Alcohol (completely avoid — wipes out B12)"}
        ));

        plans.put("High Cholesterol", new DietPlan(
            "High Cholesterol", "Flush bad cholesterol, boost the good", "🫀",
            new String[]{"Raw garlic (1 clove daily — lowers LDL)", "Raw walnuts (4–5/day)",
                "Raw flaxseeds (1 tbsp — crush before eating)", "Raw chia seeds soaked in water",
                "Raw oats (soaked overnight) — beta-glucan", "Fresh berries",
                "Raw apple with skin (pectin — binds cholesterol)",
                "Raw onion slices", "Raw avocado slices", "Soaked almonds (5–6)"},
            new String[]{"Oats + apple + flaxseeds + plant sterol milk",
                "Brown rice + dal + sabzi (olive oil) + salad",
                "Rajma + multigrain roti + cucumber",
                "Tofu stir-fry + broccoli + roti",
                "Quinoa + chickpea salad + olive oil",
                "Moong dal soup + multigrain toast"},
            new String[]{"Baked fish (salmon/tuna) + quinoa + salad",
                "Grilled chicken breast + roasted vegetables",
                "Egg whites (3) omelette + whole wheat toast",
                "Baked sardines + brown rice + greens",
                "Grilled fish + sweet potato + salad"},
            new String[]{"Saturated fats: fatty meat, full-fat dairy, butter, ghee (excess)",
                "Trans fats: vanaspati, packaged biscuits, cakes, namkeen",
                "Egg yolks: limit to 3–4/week", "Shellfish & organ meats",
                "Fried foods", "Refined carbs & sugar", "Alcohol (raises triglycerides)"},
            new String[]{"Oats porridge + walnuts + flaxseeds + green tea",
                "Whole wheat toast + avocado + fruit",
                "Oats idli + sambar (low oil)"},
            new String[]{"Brown rice + dal (olive oil) + sabzi + salad",
                "Multigrain roti + rajma + cucumber",
                "Grilled fish + quinoa + stir-fried vegetables"},
            new String[]{"Baked fish/chicken + roasted veggies",
                "Moong dal soup + roti",
                "Tofu stir-fry + brown rice"},
            new String[]{"Apple slices", "Walnuts (4–5)", "Roasted soy nuts", "Oat biscuits (plain)"},
            new String[]{"Exercise 30+ min daily — aerobic exercise lowers LDL by 10–15%",
                "Lose 5–10% of body weight — significantly improves cholesterol",
                "Quit smoking — it raises HDL (good) and helps heart",
                "Limit alcohol strictly — it raises triglycerides",
                "Get lipid profile checked every 6 months"},
            new String[]{"Egg yolks (limit — ok for B12 & Vit D3)", "Ghee (limit — ok for Thyroid in small amounts)"}
        ));
    }

    public static Map<String, DietPlan> getAllPlans() { return plans; }
    public static DietPlan getPlan(String disease) { return plans.get(disease); }
    public static String[] getAllDiseases() { return plans.keySet().toArray(new String[0]); }

    /** Returns cross-condition warnings for a set of selected diseases */
    public static List<String> getCrossWarnings(List<String> selectedDiseases) {
        List<String> warnings = new ArrayList<>();
        if (selectedDiseases.contains("Diabetes") && selectedDiseases.contains("Vitamin D3 Deficiency")) {
            warnings.add("⚠ Dairy: Good for Vit D3 but choose low-fat dairy for Diabetes.");
        }
        if (selectedDiseases.contains("Thyroid") && selectedDiseases.contains("High Cholesterol")) {
            warnings.add("⚠ Coconut oil: Used for Hypothyroid but avoid with High Cholesterol.");
        }
        if (selectedDiseases.contains("PCOS") && selectedDiseases.contains("Thyroid")) {
            warnings.add("⚠ Soy products: Avoid for both PCOS and Thyroid.");
        }
        if (selectedDiseases.contains("High Cholesterol") && selectedDiseases.contains("Vitamin B12 Deficiency")) {
            warnings.add("⚠ Egg yolks: Essential for B12 but limit to 3–4/week for Cholesterol.");
        }
        if (selectedDiseases.contains("Heart Problems") && selectedDiseases.contains("Vitamin D3 Deficiency")) {
            warnings.add("⚠ Full-fat dairy: Needed for D3 absorption but avoid for Heart Problems.");
        }
        return warnings;
    }
}
