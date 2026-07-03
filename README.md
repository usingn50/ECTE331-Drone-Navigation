# ECTE331 - مشاريع أنظمة التشغيل

يحتوي هذا المستودع على مشاريع متعددة تم تطويرها كجزء من مقرر ECTE331 (أنظمة التشغيل)، مع التركيز على مفاهيم المزامنة، إدارة الموارد، وجدولة المهام في بيئات متعددة الخيوط.

تم تقسيم العمل إلى مراحل (commits) واضحة لتتبع التقدم في كل جزء من المشروع.

## 1. مشروع نظام الملاحة للطائرات بدون طيار (Drone Navigation System)

**الملف الرئيسي:** `DroneNavigationSystem.java`

يتناول هذا المشروع محاكاة نظام ملاحة لطائرة بدون طيار، مع التركيز على دمج البيانات من حساسات متعددة واتخاذ قرارات الملاحة. تم تطويره على ثلاث مراحل رئيسية:

*   **المرحلة الأولى:** تحسين تنسيق مخرجات الكونسول واستخدام الثوابت لزيادة قابلية القراءة والصيانة.
*   **المرحلة الثانية:** تطبيق منطق اكتشاف القيم الشاذة (Outlier Detection) في قراءات الحساسات وتحسين تسجيل السجلات (logging) لتحديد الحساسات التي تنتج قيمًا شاذة.
*   **المرحلة الثالثة:** إضافة ميزة الهبوط الاضطراري عند اكتشاف أعطال حرجة في النظام، مع توفير ملخص نهائي للمحاكاة.

## 2. مشروع نظام التحكم بالذراع الروبوتية (Robotic Arm Control System)

**المجلد:** `ECTE331RoboticArm`
**الملف الرئيسي:** `ECTE331RoboticArm/RoboticArmSystem.java`

يركز هذا المشروع على محاكاة نظام تحكم بذراع روبوتية باستخدام خيوط متعددة وموارد مشتركة، مع استكشاف تحديات المزامنة مثل انعكاس الأولوية (Priority Inversion) وحلولها مثل وراثة الأولوية (Priority Inheritance) وسقف الأولوية (Priority Ceiling).

*   **المرحلة الأولى:** تنفيذ الأساس متعدد الخيوط مع مورد مشترك (MotorController) وآليات مزامنة أساسية.
*   **المرحلة الثانية:** إظهار سيناريو انعكاس الأولوية (Priority Inversion) ومحاكاة وراثة الأولوية (Priority Inheritance) لحل المشكلة.
*   **المرحلة الثالثة:** تنفيذ سقف الأولوية (Priority Ceiling) كحل آخر لانعكاس الأولوية وتقييم الأداء.

## 3. مشروع مزامنة الخيوط والاتصال (Threads Synchronisation and Communication)

**المجلد:** `ECTE331ThreadsSynchronisation`

يتناول هذا المشروع مفاهيم متقدمة في مزامنة الخيوط والاتصال بينها باستخدام Java، مع التركيز على حلول لمشاكل كلاسيكية في أنظمة التشغيل.

*   **المرحلة الأولى:** تنفيذ مشكلة المنتج-المستهلك (Producer-Consumer Problem) باستخدام كل من `synchronized` و `wait()`/`notifyAll()`، وكذلك باستخدام `ReentrantLock` و `Condition`. بالإضافة إلى تنفيذ مشكلة القارئ-الكاتب (Reader-Writer Problem) باستخدام `ReentrantReadWriteLock`.

## التوثيق

تم توليد توثيق JavaDoc لكل من مشروع نظام الملاحة ومشروع مزامنة الخيوط. يمكن العثور على التوثيق في المجلدات التالية:

*   `docs/DroneNavigationSystem`
*   `docs/ECTE331ThreadsSynchronisation`

## كيفية التشغيل

للتشغيل، تأكد من تثبيت Java Development Kit (JDK).

1.  **استنساخ المستودع:**
    ```bash
    git clone https://github.com/usingn50/ECTE331-Drone-Navigation.git
    cd ECTE331-Drone-Navigation
    ```

2.  **تجميع وتشغيل مشروع نظام الملاحة للطائرات بدون طيار:**
    ```bash
    javac DroneNavigationSystem.java SensorReading.java
    java DroneNavigationSystem
    ```

3.  **تجميع وتشغيل مشروع نظام التحكم بالذراع الروبوتية:**
    ```bash
    javac ECTE331RoboticArm/*.java
    java -cp . ECTE331RoboticArm.RoboticArmSystem
    ```

4.  **تجميع وتشغيل مشروع مزامنة الخيوط والاتصال:**
    ```bash
    javac ECTE331ThreadsSynchronisation/*.java
    java -cp . ECTE331ThreadsSynchronisation.ProducerConsumerSync
    java -cp . ECTE331ThreadsSynchronisation.ProducerConsumerLock
    java -cp . ECTE331ThreadsSynchronisation.ReaderWriterProblem
    ```

---
