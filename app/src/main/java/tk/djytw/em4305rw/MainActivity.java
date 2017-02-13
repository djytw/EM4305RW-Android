package tk.djytw.em4305rw;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position==1){
                    EditText tab2id = (EditText) findViewById(R.id.tab2id);
                    tab2id.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void onTextChanged(CharSequence s, int start, int before,int count) {
                        }
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count,int after) {
                        }
                        @Override
                        public void afterTextChanged(Editable s) {
                            LinearLayout con=(LinearLayout)findViewById(R.id.tab2con);
                            LinearLayout divider1=(LinearLayout)findViewById(R.id.tab2divider1);
                            TextView tv=(TextView)findViewById(R.id.tab2cont);
                            LinearLayout divider2=(LinearLayout)findViewById(R.id.tab2divider2);
                            TextView tv2=(TextView)findViewById(R.id.tab2cont2);
                            Button tab2ok=(Button)findViewById(R.id.tab2ok);
                            if(s.toString().length()==0) {
                                divider1.setVisibility(View.GONE);
                                tv.setVisibility(View.GONE);
                                divider2.setVisibility(View.GONE);
                                tv2.setVisibility(View.GONE);
                                tab2ok.setBackground(getResources().getDrawable(R.drawable.corner_d_i));
                                tab2ok.setText("确定");
                                return;
                            }
                            try{
                                int a=Integer.parseInt(s.toString());
                                int ka=(a&0xff0000)>>16;
                                int kb=a&0xffff;
                                tv.setText(String.format("卡片内容：%010d %03d,%05d",a,ka,kb));
                                divider1.setVisibility(View.VISIBLE);
                                tv.setVisibility(View.VISIBLE);
                                long[] ans=parseData(a);
                                tv2.setText("写入数据："+String.format("%08X %08X",ans[2],ans[3]));
                                divider2.setVisibility(View.VISIBLE);
                                tv2.setVisibility(View.VISIBLE);
                                tab2ok.setBackground(getResources().getDrawable(R.drawable.corner_d));
                                tab2ok.setText("点击写入数据（请确认卡放在读卡器上）");
                            }catch (Exception e){
                                tv.setText("请输入一个10进制数字");
                                divider1.setVisibility(View.VISIBLE);
                                tv.setVisibility(View.VISIBLE);
                                divider2.setVisibility(View.GONE);
                                tv2.setVisibility(View.GONE);
                                tab2ok.setBackground(getResources().getDrawable(R.drawable.corner_d_i));
                                tab2ok.setText("确定");
                            }
                        }
                    });
                }
            }
            public long[] parseData(int id){
                long[] ret=new long[4];
                ret[0]=ret[3]=0;
                ret[1]=0x5f800100L;
                int i,j,ecc;
                ret[2]=0x7fc00;
                int t,tt;ecc=0;

                for(i=7;i>5;i--){
                    ecc=0;
                    t=(id&0xf<<(i*4))>>(i*4);
                    for(j=3;j>=0;j--){
                        tt=(t&1<<j)>>j;
                        ecc+=tt;
                        ret[2]<<=1;
                        ret[2]+=tt;
                    }
                    ret[2]<<=1;
                    ret[2]+=ecc&1;
                }
                ecc=0;
                t=(id&0xf<<(5*4))>>(5*4);
                for(j=3;j>0;j--){
                    tt=(t&1<<j)>>j;
                    ecc+=tt;
                    ret[2]<<=1;
                    ret[2]+=tt;
                }
                //ret2 ok
                tt=t&1;
                ecc+=tt;
                ret[3]=tt;
                ret[3]<<=1;
                ret[3]+=ecc&1;

                for(i=4;i>=0;i--){
                    ecc=0;
                    t=(id&0xf<<(i*4))>>(i*4);
                    for(j=3;j>=0;j--){
                        tt=(t&1<<j)>>j;
                        ecc+=tt;
                        ret[3]<<=1;
                        ret[3]+=tt;
                    }
                    ret[3]<<=1;
                    ret[3]+=ecc&1;
                }
                for(i=3;i>=0;i--){
                    ecc=0;
                    for(j=7;j>=0;j--){
                        ecc+=(id&1<<(j*4+i))>>(j*4+i);
                    }
                    ret[3]<<=1;
                    ret[3]+=ecc&1;
                }
                ret[3]<<=1;
                System.out.println(ret[2]+" "+ret[3]);
                //parse ok,reverse
                int ch[]={0,8,4,12,2,10,6,14,1,9,5,13,3,12,7,15};
                String str=String.format("%08X",ret[2]);
                ret[2]=0;
                for(i=0;i<7;i+=2){
                    if(Character.isDigit(str.charAt(i)))t=str.charAt(i)-'0';
                    else t=str.charAt(i)-'A'+10;
                    if(Character.isDigit(str.charAt(i+1)))tt=str.charAt(i+1)-'0';
                    else tt=str.charAt(i+1)-'A'+10;
                    System.out.println("t:"+t+" tt:"+tt+"cht:"+ch[t]+"chtt:"+ch[tt]);

                    ret[2]<<=4;
                    ret[2]+=ch[tt];
                    ret[2]<<=4;
                    ret[2]+=ch[t];
                }
                str=String.format("%08X",ret[3]);
                ret[3]=0;
                for(i=0;i<7;i+=2){
                    if(Character.isDigit(str.charAt(i)))t=str.charAt(i)-'0';
                    else t=str.charAt(i)-'A'+10;
                    if(Character.isDigit(str.charAt(i+1)))tt=str.charAt(i+1)-'0';
                    else tt=str.charAt(i+1)-'A'+10;
                    System.out.println("t:"+t+" tt:"+tt+"cht:"+ch[t]+"chtt:"+ch[tt]);
                    ret[3]<<=4;
                    ret[3]+=ch[tt];
                    ret[3]<<=4;
                    ret[3]+=ch[t];
                }System.out.println("final:"+ret[2]+" "+ret[3]);
                return ret;
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.action_edit:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.action_share:
                mViewPager.setCurrentItem(1);
                break;
            case R.id.action_settings:
                mViewPager.setCurrentItem(2);
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView ;
            switch(getArguments().getInt(ARG_SECTION_NUMBER)){
                case 1:
                    rootView = inflater.inflate(R.layout.tab1, container, false);
                    break;
                case 2:
                    rootView = inflater.inflate(R.layout.tab2, container, false);
                    break;
                default:
                    rootView = inflater.inflate(R.layout.tab3, container, false);
                    break;
            }
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }
}
