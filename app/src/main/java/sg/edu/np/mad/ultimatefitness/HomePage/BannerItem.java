package sg.edu.np.mad.ultimatefitness.HomePage;


    public class BannerItem {
        private int imageResourceId;
        private String title;
        private String subtitle;
        private Class<?> targetActivity;

        public BannerItem(int imageResourceId, String title, String subtitle, Class<?> targetActivity) {
            this.imageResourceId = imageResourceId;
            this.title = title;
            this.subtitle = subtitle;
            this.targetActivity = targetActivity;
        }

        public int getImageResourceId() {
            return imageResourceId;
        }

        public String getTitle() {
            return title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public Class<?> getTargetActivity() {
            return targetActivity;
        }
    }
