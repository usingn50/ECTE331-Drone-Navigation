# ECTE331 - مشاريع أنظمة التشغيل

يحتوي هذا المستودع على مشاريع متعددة تم تطويرها كجزء من مقرر ECTE331 (أنظمة التشغيل / الأنظمة المدمجة)، مع التركيز على التحمل من الأعطال، إدارة الموارد المشتركة، ومزامنة الخيوط في بيئات متعددة الخيوط.

## 1. مشروع نظام الملاحة للطائرات بدون طيار (Drone Navigation System)

**الملف الرئيسي:** `DroneNavigationSystem.java`
**الاستثناءات المخصصة:** `SensorReadException.java`, `SystemReliabilityException.java`

يحاكي هذا المشروع نظام ملاحة لطائرة بدون طيار يعتمد على تقنية التكرار الثلاثي (Triple Modular Redundancy - TMR) لتحديد الارتفاع باستخدام ثلاثة حساسات، مع:

- محاكاة فشل/تلف/صحة القراءات حسب نسب عشوائية محددة
- تصويت الأغلبية (Majority Voting) لتحديد القراءة النهائية
- الرجوع للقيمة السابقة (Fallback) عند تعارض جميع القراءات
- الدخول في وضع السلامة (SAFE MODE) عند فشلين متتاليين في الموثوقية
- تسجيل كل الأحداث المهمة في ملف `log.txt`

تقرير كامل مع أدلة فعلية من السجل لكل حالة استخدام مطلوبة: `Report_Part1.md`

## 2. مشروع نظام التحكم بالذراع الروبوتية (Robotic Arm Control System)

**المجلد:** `ECTE331RoboticArm`

يحاكي هذا المشروع نظام تحكم بذراع روبوتية بثلاثة خيوط ذات أولويات مختلفة (High: `SafetyMonitor`, Medium: `MotionPlanner`, Low: `Logger`) تتشارك موردًا واحدًا (`MotorController`)، ويغطي المتطلبات الستة كاملة:

| Task | الملفات | الوصف |
|---|---|---|
| 1-2 | `RoboticArmSystem.java` | تنفيذ أساسي متعدد الخيوط مع إقصاء متبادل (mutual exclusion) على المورد المشترك |
| 3 | `PriorityInversionDemo.java`, `LowPriorityTask.java`, `MediumPriorityTask.java` | سيناريو متحكَّم فيه يُظهر انعكاس الأولوية (Priority Inversion) بشكل قابل لإعادة الإنتاج |
| 4 | `MotorController.java` (`PriorityMode.INHERITANCE`) | محاكاة وراثة الأولوية (Priority Inheritance) |
| 5 | `MotorController.java` (`PriorityMode.CEILING`) | تنفيذ بروتوكول سقف الأولوية (Priority Ceiling) |
| 6 | `PerformanceEvaluator.java`, `performance_results.csv`, `performance_chart.png` | تقييم أداء الاستراتيجيات الثلاث على 20 تجربة، مع جداول ورسم بياني |

تقرير كامل يغطي التصميم والمهام 1-6 مع أدلة فعلية من السجلات: `ECTE331RoboticArm/Report_RoboticArm.md`

**ملاحظة منهجية:** بما أن أولويات الخيوط في Java تُعامَل كـ"تلميح" للـ JVM/نظام التشغيل وليست ضمانًا حقيقيًا للجدولة الفورية على أنظمة التشغيل العامة، فقد تم تصميم آلية "تداخل" (`setMediumInterfering`) داخل `MotorController` لمحاكاة تأثير انعكاس الأولوية بشكل حتمي وقابل لإعادة الإنتاج على أي جهاز، بدل الاعتماد الكامل على سلوك المجدول الفعلي — راجع Javadoc الخاص بـ `MotorController` للتفاصيل.

## 3. مشروع مزامنة الخيوط والاتصال (Threads Synchronisation and Communication)

**المجلد:** `ECTE331ThreadsSynchronisation`

يحل هذا المشروع "Problem 2" من المقرر: خيطان (A وB) بترتيب تنفيذ محدد بين ست دوال (FuncA1-3, FuncB1-3) تتشارك ست متغيرات، بدون استخدام busy-wait أو `Thread.sleep()` لفرض الترتيب.

- `SumUtil.java` — دالة مساعدة لحساب المجموع باستخدام حلقة تكرار
- `SyncEvent.java` — بوابة مزامنة أحادية الاستخدام مبنية على `synchronized`/`wait()`/`notifyAll()`
- `SharedState.java` — يحمل المتغيرات المشتركة وأحداث المزامنة الأربعة
- `ThreadA.java`, `ThreadB.java` — تنفيذ الخيطين حسب ترتيب الاعتماد في Figure 2.1
- `ThreadSyncApp.java` — تشغيل توضيحي + اختبار صحة على 100,000 تكرار متتالي
- `Report_Part2.md` — تقرير كامل يشرح الحل الرياضي وتصميم المزامنة ونتائج الاختبار

## كيفية التشغيل

للتشغيل، تأكد من تثبيت Java Development Kit (JDK 17 أو أحدث).

1.  **استنساخ المستودع:**
    ```bash
    git clone https://github.com/usingn50/ECTE331-Drone-Navigation.git
    cd ECTE331-Drone-Navigation
    ```

2.  **مشروع نظام الملاحة للطائرات بدون طيار:**
    ```bash
    javac DroneNavigationSystem.java SensorReadException.java SystemReliabilityException.java
    java DroneNavigationSystem
    ```

3.  **مشروع نظام التحكم بالذراع الروبوتية:**
    ```bash
    javac ECTE331RoboticArm/*.java
    java -cp . ECTE331RoboticArm.RoboticArmSystem        # Task 1-2: العرض الأساسي
    java -cp . ECTE331RoboticArm.PriorityInversionDemo    # Task 3-5: عرض انعكاس الأولوية والحلول
    java -cp . ECTE331RoboticArm.PerformanceEvaluator 20  # Task 6: تقييم الأداء (20 تجربة)
    ```

4.  **مشروع مزامنة الخيوط والاتصال:**
    ```bash
    cd ECTE331ThreadsSynchronisation
    javac *.java
    java ThreadSyncApp
    ```

## التوثيق

يوجد توثيق JavaDoc لمشروع نظام الملاحة في `docs/DroneNavigationSystem`. تقارير كل جزء موجودة كملفات Markdown داخل مجلد كل مشروع.
