package medbuddy;

import java.util.*;
import java.time.LocalDate;

/**
 * Rule-based ML-style Diet Engine.
 * Analyses the user's current progress metrics (weight trend, sugar, cholesterol,
 * vitamins) and produces a personalised severity score + tailored weekly meal plan.
 *
 * Scoring model:
 *   - Each metric is mapped to a normalised severity 0-3 (0=healthy, 3=critical)
 *   - A weighted sum produces an overall HealthScore
 *   - Diet strictness tier (MAINTENANCE / THERAPEUTIC / STRICT) is chosen per score
 *   - Weekly plan rotates through tier-appropriate meals day-by-day
 */
public class MLDietEngine {

    // ── Public result types ───────────────────────────────────────────

    public enum Tier { MAINTENANCE, THERAPEUTIC, STRICT }

    public static class MetricStatus {
        public final String name, value, status, advice;
        public final Color color; // "green" | "amber" | "red"
        public MetricStatus(String name, String value, String status, String advice, Color color) {
            this.name = name; this.value = value; this.status = status;
            this.advice = advice; this.color = color;
        }
        public enum Color { GREEN, AMBER, RED }
    }

    public static class DayMealPlan {
        public final String day;
        public final String breakfast, morningSnack, lunch, afternoonSnack, dinner, eveningTip;
        public DayMealPlan(String day, String breakfast, String morningSnack,
                           String lunch, String afternoonSnack, String dinner, String eveningTip) {
            this.day = day; this.breakfast = breakfast; this.morningSnack = morningSnack;
            this.lunch = lunch; this.afternoonSnack = afternoonSnack;
            this.dinner = dinner; this.eveningTip = eveningTip;
        }
    }

    public static class Recommendation {
        public final Tier tier;
        public final String tierLabel, tierReason;
        public final List<MetricStatus> metricStatuses;
        public final List<DayMealPlan> weeklyPlan;     // Mon–Sun
        public final List<String> personalInsights;    // ML-derived insights
        public final double overallScore;              // 0-100 (lower=healthier)

        public Recommendation(Tier tier, String tierLabel, String tierReason,
                              List<MetricStatus> metricStatuses, List<DayMealPlan> weeklyPlan,
                              List<String> personalInsights, double overallScore) {
            this.tier = tier; this.tierLabel = tierLabel; this.tierReason = tierReason;
            this.metricStatuses = metricStatuses; this.weeklyPlan = weeklyPlan;
            this.personalInsights = personalInsights; this.overallScore = overallScore;
        }
    }

    // ── Public API ───────────────────────────────────────────────────

    /**
     * Analyse the current user and return a full recommendation.
     * Uses the most recent progress entry + baseline health profile.
     */
    public static Recommendation analyse(UserStore.User user) {
        UserStore.HealthProfile p = user.profile;
        List<UserStore.ProgressEntry> log = p.progressLog;

        // Pull latest values (fallback to baseline profile values)
        double latestWeight   = latestNonZero(log, "weight",      p.weightKg);
        double latestSugar    = latestNonZero(log, "sugar",       p.sugarLevel);
        double latestChol     = latestNonZero(log, "cholesterol", p.cholesterolLevel);
        double latestVitD3    = latestNonZero(log, "vitd3",       p.vitaminD3);
        double latestVitB12   = latestNonZero(log, "vitb12",      p.vitaminB12);

        double bmi = (p.heightCm > 0) ? latestWeight / Math.pow(p.heightCm / 100.0, 2) : 0;

        // ── Score each metric ────────────────────────────────────────
        List<MetricStatus> statuses = new ArrayList<>();
        double totalScore = 0;

        // Blood sugar: normal fasting < 100, pre-diabetic 100-125, diabetic ≥ 126
        if (latestSugar > 0) {
            double s; MetricStatus.Color c; String st, advice;
            if (latestSugar < 100) {
                s = 0; c = MetricStatus.Color.GREEN;
                st = "Normal (" + (int)latestSugar + " mg/dL)";
                advice = "Great! Maintain low-glycaemic foods and avoid sugary snacks.";
            } else if (latestSugar < 126) {
                s = 1.5; c = MetricStatus.Color.AMBER;
                st = "Pre-diabetic (" + (int)latestSugar + " mg/dL)";
                advice = "Reduce refined carbs, add bitter gourd juice & methi seeds daily.";
            } else {
                s = 3; c = MetricStatus.Color.RED;
                st = "High (" + (int)latestSugar + " mg/dL)";
                advice = "Strict low-GI diet essential. Consult doctor immediately.";
            }
            statuses.add(new MetricStatus("Blood Sugar", (int)latestSugar + " mg/dL", st, advice, c));
            totalScore += s * 1.5;  // weight: high impact
        }

        // Cholesterol: normal < 200, borderline 200-239, high ≥ 240
        if (latestChol > 0) {
            double s; MetricStatus.Color c; String st, advice;
            if (latestChol < 200) {
                s = 0; c = MetricStatus.Color.GREEN;
                st = "Optimal (" + (int)latestChol + " mg/dL)";
                advice = "Keep including omega-3 foods like flaxseeds and walnuts.";
            } else if (latestChol < 240) {
                s = 1.5; c = MetricStatus.Color.AMBER;
                st = "Borderline (" + (int)latestChol + " mg/dL)";
                advice = "Cut fried foods, increase soluble fibre (oats, barley, dal).";
            } else {
                s = 3; c = MetricStatus.Color.RED;
                st = "High (" + (int)latestChol + " mg/dL)";
                advice = "Eliminate trans fats & saturated fats. Add oats daily.";
            }
            statuses.add(new MetricStatus("Cholesterol", (int)latestChol + " mg/dL", st, advice, c));
            totalScore += s * 1.2;
        }

        // BMI: underweight <18.5, normal 18.5-24.9, overweight 25-29.9, obese ≥30
        if (bmi > 0) {
            double s; MetricStatus.Color c; String st, advice;
            String bmiStr = String.format("%.1f", bmi);
            if (bmi < 18.5) {
                s = 1.5; c = MetricStatus.Color.AMBER;
                st = "Underweight (BMI " + bmiStr + ")";
                advice = "Increase calorie-dense healthy foods: nuts, ghee (1 tsp), avocado.";
            } else if (bmi < 25) {
                s = 0; c = MetricStatus.Color.GREEN;
                st = "Healthy (BMI " + bmiStr + ")";
                advice = "Excellent! Focus on maintaining this range with balanced meals.";
            } else if (bmi < 30) {
                s = 1.5; c = MetricStatus.Color.AMBER;
                st = "Overweight (BMI " + bmiStr + ")";
                advice = "Reduce evening carbs, add 30-min walk after dinner.";
            } else {
                s = 3; c = MetricStatus.Color.RED;
                st = "Obese (BMI " + bmiStr + ")";
                advice = "Calorie deficit diet required. Prioritise protein + vegetables.";
            }
            statuses.add(new MetricStatus("BMI", bmiStr, st, advice, c));
            totalScore += s * 1.0;
        }

        // Vitamin D3: deficient <20, insufficient 20-29, sufficient ≥30
        if (latestVitD3 > 0) {
            double s; MetricStatus.Color c; String st, advice;
            if (latestVitD3 < 20) {
                s = 2; c = MetricStatus.Color.RED;
                st = "Deficient (" + (int)latestVitD3 + " ng/mL)";
                advice = "Add egg yolk, mushrooms, fortified milk. Get 20 min of sunlight daily.";
            } else if (latestVitD3 < 30) {
                s = 1; c = MetricStatus.Color.AMBER;
                st = "Insufficient (" + (int)latestVitD3 + " ng/mL)";
                advice = "Morning sunlight + fatty fish or D3-fortified foods weekly.";
            } else {
                s = 0; c = MetricStatus.Color.GREEN;
                st = "Sufficient (" + (int)latestVitD3 + " ng/mL)";
                advice = "Good! Maintain with regular sun exposure and dairy.";
            }
            statuses.add(new MetricStatus("Vitamin D3", (int)latestVitD3 + " ng/mL", st, advice, c));
            totalScore += s * 0.7;
        }

        // Vitamin B12: deficient <200, normal 200-900
        if (latestVitB12 > 0) {
            double s; MetricStatus.Color c; String st, advice;
            if (latestVitB12 < 200) {
                s = 2; c = MetricStatus.Color.RED;
                st = "Deficient (" + (int)latestVitB12 + " pg/mL)";
                advice = "Critical: Add paneer, curd, eggs, fish. Consider B12 supplement.";
            } else if (latestVitB12 < 300) {
                s = 0.8; c = MetricStatus.Color.AMBER;
                st = "Low-Normal (" + (int)latestVitB12 + " pg/mL)";
                advice = "Include dairy, eggs or fish 4-5 times a week.";
            } else {
                s = 0; c = MetricStatus.Color.GREEN;
                st = "Normal (" + (int)latestVitB12 + " pg/mL)";
                advice = "Well maintained. Continue current dairy/protein intake.";
            }
            statuses.add(new MetricStatus("Vitamin B12", (int)latestVitB12 + " pg/mL", st, advice, c));
            totalScore += s * 0.7;
        }

        // Normalise score 0-100
        double maxPossible = 5 * 3.0 * 1.5; // rough max
        double normScore = Math.min(100, (totalScore / maxPossible) * 100);

        // ── Determine tier ────────────────────────────────────────────
        Tier tier; String tierLabel, tierReason;
        if (normScore < 20) {
            tier = Tier.MAINTENANCE;
            tierLabel = "Maintenance Diet";
            tierReason = "Your metrics look healthy. Focus on sustaining your current habits.";
        } else if (normScore < 55) {
            tier = Tier.THERAPEUTIC;
            tierLabel = "Therapeutic Diet";
            tierReason = "Some metrics need attention. A targeted diet can reverse these trends.";
        } else {
            tier = Tier.STRICT;
            tierLabel = "Strict Medical Diet";
            tierReason = "Multiple metrics are in concerning range. A disciplined plan is critical.";
        }

        // ── Build weekly plan ─────────────────────────────────────────
        List<String> conditions = p.conditions;
        List<DayMealPlan> weeklyPlan = buildWeeklyPlan(tier, conditions, latestSugar, latestChol, bmi, latestVitD3, latestVitB12);

        // ── Personal insights ─────────────────────────────────────────
        List<String> insights = buildInsights(log, latestSugar, latestChol, bmi, latestVitD3, latestVitB12, user.age, user.gender, conditions);

        return new Recommendation(tier, tierLabel, tierReason, statuses, weeklyPlan, insights, normScore);
    }

    // ── Weekly meal plan builder ──────────────────────────────────────

    private static List<DayMealPlan> buildWeeklyPlan(Tier tier, List<String> conditions,
            double sugar, double chol, double bmi, double vitD3, double vitB12) {

        boolean diabetic    = conditions.stream().anyMatch(c -> c.equalsIgnoreCase("Diabetes"));
        boolean hasPcos     = conditions.stream().anyMatch(c -> c.equalsIgnoreCase("PCOS"));
        boolean hasThyroid  = conditions.stream().anyMatch(c -> c.equalsIgnoreCase("Thyroid"));
        boolean hasHyper    = conditions.stream().anyMatch(c -> c.toLowerCase().contains("hypertension") || c.toLowerCase().contains("bp"));
        boolean highChol    = chol >= 200;
        boolean highSugar   = sugar >= 100;
        boolean vitD3Low    = vitD3 > 0 && vitD3 < 30;
        boolean vitB12Low   = vitB12 > 0 && vitB12 < 300;
        boolean overweight  = bmi >= 25;

        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        List<DayMealPlan> plan = new ArrayList<>();

        // 7 varied breakfasts
        String[] breakfasts = {
            buildBreakfast(0, diabetic, hasPcos, highSugar, vitD3Low, vitB12Low, tier),
            buildBreakfast(1, diabetic, hasPcos, highSugar, vitD3Low, vitB12Low, tier),
            buildBreakfast(2, diabetic, hasPcos, highSugar, vitD3Low, vitB12Low, tier),
            buildBreakfast(3, diabetic, hasPcos, highSugar, vitD3Low, vitB12Low, tier),
            buildBreakfast(4, diabetic, hasPcos, highSugar, vitD3Low, vitB12Low, tier),
            buildBreakfast(5, diabetic, hasPcos, highSugar, vitD3Low, vitB12Low, tier),
            buildBreakfast(6, diabetic, hasPcos, highSugar, vitD3Low, vitB12Low, tier)
        };
        String[] morningSnacks = {
            "5–6 soaked almonds + green tea (no sugar)",
            "1 small apple + 1 tbsp pumpkin seeds",
            "Roasted makhana (lotus seeds) - 1 handful",
            "Cucumber sticks + hummus (2 tbsp)",
            "Handful of walnuts (4–5 pieces)",
            "Amla juice (30 ml) + 5 soaked almonds",
            "1 small pear + herbal spearmint tea"
        };
        String[] lunches = {
            buildLunch(0, diabetic, hasPcos, highChol, highSugar, hasHyper, overweight, tier),
            buildLunch(1, diabetic, hasPcos, highChol, highSugar, hasHyper, overweight, tier),
            buildLunch(2, diabetic, hasPcos, highChol, highSugar, hasHyper, overweight, tier),
            buildLunch(3, diabetic, hasPcos, highChol, highSugar, hasHyper, overweight, tier),
            buildLunch(4, diabetic, hasPcos, highChol, highSugar, hasHyper, overweight, tier),
            buildLunch(5, diabetic, hasPcos, highChol, highSugar, hasHyper, overweight, tier),
            buildLunch(6, diabetic, hasPcos, highChol, highSugar, hasHyper, overweight, tier)
        };
        String[] afternoonSnacks = {
            "Karela (bitter gourd) juice 30 ml" + (diabetic ? " ← key for sugar control" : ""),
            "Low-fat curd (1 cup) + cucumber slices",
            "Roasted chana (1 small handful)",
            "1 banana" + (diabetic ? " (⚠ skip — high sugar)" : " + 5 almonds"),
            "Sprout salad (moong + chana, 1 small bowl)",
            "Buttermilk (chaas, no salt) 1 glass",
            "2 rice crackers + peanut butter (1 tsp)"
        };
        String[] dinners = {
            buildDinner(0, diabetic, hasPcos, highChol, hasHyper, overweight, vitD3Low, vitB12Low, tier),
            buildDinner(1, diabetic, hasPcos, highChol, hasHyper, overweight, vitD3Low, vitB12Low, tier),
            buildDinner(2, diabetic, hasPcos, highChol, hasHyper, overweight, vitD3Low, vitB12Low, tier),
            buildDinner(3, diabetic, hasPcos, highChol, hasHyper, overweight, vitD3Low, vitB12Low, tier),
            buildDinner(4, diabetic, hasPcos, highChol, hasHyper, overweight, vitD3Low, vitB12Low, tier),
            buildDinner(5, diabetic, hasPcos, highChol, hasHyper, overweight, vitD3Low, vitB12Low, tier),
            buildDinner(6, diabetic, hasPcos, highChol, hasHyper, overweight, vitD3Low, vitB12Low, tier)
        };
        String[] eveningTips = {
            "🚶 30-min walk after dinner helps lower blood sugar by 15–20 mg/dL",
            "💧 Drink 1 glass warm water with lemon before sleeping — aids digestion",
            "🧘 10 min light stretching before bed reduces cortisol levels",
            "🌙 Sleep by 10:30 PM — late sleep disrupts insulin sensitivity",
            "🫁 5 deep breaths before sleep lowers BP and stress hormones",
            "📵 Avoid screens 1 hour before bed — blue light worsens hormonal imbalance",
            "🍵 Chamomile or ashwagandha tea 30 min before sleep improves recovery"
        };

        for (int i = 0; i < 7; i++) {
            plan.add(new DayMealPlan(days[i], breakfasts[i], morningSnacks[i],
                    lunches[i], afternoonSnacks[i], dinners[i], eveningTips[i]));
        }
        return plan;
    }

    private static String buildBreakfast(int day, boolean diabetic, boolean hasPcos,
            boolean highSugar, boolean vitD3Low, boolean vitB12Low, Tier tier) {
        String[][] options = {
            // day 0 Mon
            { "Oats porridge (½ cup rolled oats, no sugar) + 5 soaked almonds + green tea",
              "Besan (chickpea) chilla (2) + green chutney + herbal tea",
              "Besan chilla (2) + green chutney + 1 boiled egg" },
            // day 1 Tue
            { "Whole wheat toast (2) + peanut butter (1 tsp) + cucumber slices",
              "Moong dal cheela (2) + low-fat curd + green tea",
              "Whole wheat toast (2) + 2-egg omelette (no oil) + tea" },
            // day 2 Wed
            { "Methi (fenugreek) thepla (2) + low-fat curd (½ cup)",
              "Ragi (finger millet) porridge + flaxseed powder (1 tsp) + green tea",
              "Ragi porridge + boiled egg (1) + green tea" },
            // day 3 Thu
            { "Vegetable upma (semolina, ½ cup) with peas + green chutney",
              "Quinoa porridge + berries (½ cup) + flaxseeds",
              "Multigrain toast (2) + scrambled eggs (2) + tomato" },
            // day 4 Fri
            { "Brown rice poha with vegetables + curry leaves + lemon",
              "Overnight oats (rolled oats + chia seeds + low-fat milk, no sugar)",
              "Overnight oats + 2 boiled eggs" },
            // day 5 Sat
            { "Idli (2, small) + sambar (1 bowl) + coconut chutney (1 tbsp)",
              "Paneer bhurji (low-oil, 50g) + multigrain roti (1) + green tea",
              "2-egg bhurji + multigrain roti (1) + tea" },
            // day 6 Sun
            { "Daliya (broken wheat) porridge + soaked almonds + herbal tea",
              "Moong dal khichdi (1 bowl, light) + amla chutney",
              "Daliya porridge + smoked salmon (50g) or boiled egg" }
        };
        int tier_idx = (tier == Tier.MAINTENANCE) ? 0 : (tier == Tier.THERAPEUTIC) ? 1 : 2;
        String base = options[day][tier_idx];
        // Personalise further
        if (vitD3Low && day % 2 == 0) base += " + 1 fortified milk glass (vit-D)";
        if (vitB12Low && day % 3 == 0) base += " + 1 small bowl curd";
        return base;
    }

    private static String buildLunch(int day, boolean diabetic, boolean hasPcos,
            boolean highChol, boolean highSugar, boolean hasHyper, boolean overweight, Tier tier) {
        String[] maintOpts = {
            "Brown rice (1 cup) + moong dal + lauki sabzi + cucumber raita",
            "Multigrain roti (2) + chana masala + onion-tomato salad",
            "Khichdi (moong + rice) + low-fat curd + salad",
            "Brown rice + rajma (kidney bean) curry + salad",
            "Quinoa bowl + palak dal + mixed vegetable sabzi",
            "Roti (2, wheat) + arhar dal + bottle gourd sabzi + salad",
            "Brown rice pulao (light) + low-fat raita + salad"
        };
        String[] therOpts = {
            "Multigrain roti (1–2) + moong dal + karela sabzi + salad",
            "Oats khichdi + low-fat curd + cucumber-tomato salad",
            "Brown rice (½ cup) + palak dal + bottle gourd + salad",
            "Roti (1) + rajma (small bowl) + sprout salad + buttermilk",
            "Quinoa upma + mixed dal + green salad",
            "Multigrain roti (1) + chana masala + onion-tomato salad",
            "Barley khichdi + low-fat curd + karela/lauki sabzi"
        };
        String[] strictOpts = {
            "Multigrain roti (1) + moong dal soup + karela sabzi + large salad (no dressing)",
            "Oats porridge (savoury) + sprout salad + buttermilk",
            "Brown rice (½ cup) + dal palak (thin) + steamed vegetable platter",
            "Roti (1) + baingan (brinjal) bharta + moong soup + salad",
            "Quinoa bowl + mixed vegetable stir-fry (no oil) + dal",
            "Sorghum (jowar) roti (1) + dal + lauki sabzi + salad",
            "Millet khichdi (small bowl) + curd (low-fat) + large salad"
        };
        String[] opts = (tier == Tier.MAINTENANCE) ? maintOpts
                      : (tier == Tier.THERAPEUTIC)  ? therOpts : strictOpts;
        String lunch = opts[day];
        if (hasHyper) lunch = lunch.replace("raita", "raita (no salt)").replace("salad", "salad (no salt)");
        return lunch;
    }

    private static String buildDinner(int day, boolean diabetic, boolean hasPcos,
            boolean highChol, boolean hasHyper, boolean overweight,
            boolean vitD3Low, boolean vitB12Low, Tier tier) {
        String[] maintOpts = {
            "Grilled paneer (75g) + stir-fried vegetables + 1 roti",
            "Moong dal soup (1 bowl) + 1 multigrain roti + salad",
            "Vegetable daliya + low-fat curd + salad",
            "Tofu stir-fry with bell peppers + 1 roti",
            "Palak paneer (low-oil) + 1 roti + salad",
            "Dal tadka + 1 roti + sautéed greens",
            "Mixed vegetable khichdi + curd"
        };
        String[] therOpts = {
            "Moong dal soup + 1 roti + large salad",
            "Grilled paneer (50g) + steamed broccoli + 1 roti",
            "Vegetable soup (no cream) + 1 multigrain roti",
            "Dal palak (thin) + 1 roti + salad",
            "Tofu scramble + stir-fried vegetables + 1 small roti",
            "Lauki (bottle gourd) sabzi + moong dal + 1 roti",
            "Methi dal + 1 roti + cucumber-tomato salad"
        };
        String[] strictOpts = {
            "Moong dal soup (clear, 1 bowl) + steamed vegetables + salad",
            "Vegetable soup (thin, no cream) + 1 small roti + salad",
            "Paneer (30g, grilled) + large salad + 1 roti",
            "Dal (thin moong) + steamed spinach + 1 roti",
            "Tofu soup + sautéed greens",
            "Karela sabzi + moong dal + 1 small multigrain roti",
            "Mixed vegetable soup + 1 small roti + salad"
        };
        String[] opts = (tier == Tier.MAINTENANCE) ? maintOpts
                      : (tier == Tier.THERAPEUTIC)  ? therOpts : strictOpts;
        String dinner = opts[day];
        if (vitB12Low) dinner += " + 1 cup low-fat milk (B12 boost)";
        if (vitD3Low && day == 4) dinner += " + mushroom sauté (D3 source)";
        return dinner;
    }

    // ── Insight builder ────────────────────────────────────────────────

    private static List<String> buildInsights(List<UserStore.ProgressEntry> log,
            double sugar, double chol, double bmi, double vitD3, double vitB12,
            int age, String gender, List<String> conditions) {
        List<String> insights = new ArrayList<>();

        // Trend analysis
        if (log.size() >= 2) {
            UserStore.ProgressEntry latest = log.get(log.size() - 1);
            UserStore.ProgressEntry prev   = log.get(log.size() - 2);
            if (latest.weight > 0 && prev.weight > 0) {
                double wDiff = latest.weight - prev.weight;
                if (wDiff > 0.5) insights.add("📈 Weight increased by " + String.format("%.1f", wDiff) + " kg since last entry — reduce evening carbs & add a 30-min walk.");
                else if (wDiff < -0.5) insights.add("📉 Weight decreased by " + String.format("%.1f", Math.abs(wDiff)) + " kg — great progress! Maintain current calorie intake.");
                else insights.add("⚖ Weight is stable — good consistency! Fine-tune portions for your goal.");
            }
            if (latest.sugar > 0 && prev.sugar > 0) {
                double sDiff = latest.sugar - prev.sugar;
                if (sDiff > 10) insights.add("🩸 Blood sugar rose by " + (int)sDiff + " mg/dL — cut white rice, bread & sugary drinks immediately.");
                else if (sDiff < -10) insights.add("✅ Blood sugar dropped by " + (int)Math.abs(sDiff) + " mg/dL — your diet changes are working!");
            }
            if (latest.cholesterol > 0 && prev.cholesterol > 0) {
                double cDiff = latest.cholesterol - prev.cholesterol;
                if (cDiff > 10) insights.add("⚠ Cholesterol increased by " + (int)cDiff + " mg/dL — eliminate fried food & add oats daily.");
                else if (cDiff < -10) insights.add("✅ Cholesterol fell by " + (int)Math.abs(cDiff) + " mg/dL — omega-3 and fibre intake is paying off!");
            }
        } else if (log.isEmpty()) {
            insights.add("📋 No progress entries yet — log your first entry to unlock personalised trend insights.");
        }

        // Static metric insights
        if (sugar >= 126) insights.add("🩸 Fasting sugar ≥126 mg/dL — clinical diabetes range. Bitter gourd juice + methi water every morning.");
        if (chol >= 240) insights.add("💔 Cholesterol ≥240 — add 1 bowl oats daily + 1 tbsp flaxseed powder. Avoid ghee temporarily.");
        if (vitD3 > 0 && vitD3 < 20) insights.add("☀ Vitamin D3 critically low — 20 min morning sunlight (8–10 AM), eat fortified milk & mushrooms.");
        if (vitB12 > 0 && vitB12 < 200) insights.add("🧠 B12 very low — neurological impact possible. Prioritise curd, paneer, eggs or see a doctor.");
        if (bmi >= 30) insights.add("⚠ BMI indicates obesity — calorie deficit of ~300 kcal/day recommended. Skip sugar & fried foods.");

        // Condition-specific
        boolean hasPcos = conditions.stream().anyMatch(c -> c.equalsIgnoreCase("PCOS"));
        boolean hasThyroid = conditions.stream().anyMatch(c -> c.equalsIgnoreCase("Thyroid"));
        if (hasPcos && gender != null && gender.equalsIgnoreCase("Female"))
            insights.add("🌸 PCOS: Eat every 3–4 hours to keep insulin stable. Spearmint tea twice daily helps hormone balance.");
        if (hasThyroid)
            insights.add("🦋 Thyroid: Avoid raw cabbage, cauliflower & soy — these interfere with iodine absorption.");
        if (age >= 50)
            insights.add("🧓 Age 50+: Prioritise calcium (curd, ragi, sesame), Vit D3 and B12 — absorption declines with age.");

        if (insights.isEmpty())
            insights.add("✅ All metrics look healthy! Your personalised plan focuses on maintenance and prevention.");

        return insights;
    }

    // ── Helper ────────────────────────────────────────────────────────

    private static double latestNonZero(List<UserStore.ProgressEntry> log, String field, double fallback) {
        for (int i = log.size() - 1; i >= 0; i--) {
            UserStore.ProgressEntry e = log.get(i);
            double v = switch (field) {
                case "weight" -> e.weight;
                case "sugar"  -> e.sugar;
                case "cholesterol" -> e.cholesterol;
                case "vitd3"  -> e.vitaminD3;
                case "vitb12" -> e.vitaminB12;
                default -> 0;
            };
            if (v > 0) return v;
        }
        return fallback;
    }
}
