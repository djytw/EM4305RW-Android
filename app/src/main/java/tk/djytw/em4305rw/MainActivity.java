package tk.djytw.em4305rw;


import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
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

import org.w3c.dom.Text;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;

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
                if (position == 1) {
                    EditText tab2id = (EditText) findViewById(R.id.tab2id);
                    tab2id.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                        }

                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            LinearLayout con = (LinearLayout) findViewById(R.id.tab2con);
                            LinearLayout divider1 = (LinearLayout) findViewById(R.id.tab2divider1);
                            TextView tv = (TextView) findViewById(R.id.tab2cont);
                            LinearLayout divider2 = (LinearLayout) findViewById(R.id.tab2divider2);
                            TextView tv2 = (TextView) findViewById(R.id.tab2cont2);
                            Button tab2ok = (Button) findViewById(R.id.tab2ok);
                            if (s.toString().length() == 0) {
                                divider1.setVisibility(View.GONE);
                                tv.setVisibility(View.GONE);
                                divider2.setVisibility(View.GONE);
                                tv2.setVisibility(View.GONE);
                                tab2ok.setBackground(getResources().getDrawable(R.drawable.corner_d_i));
                                tab2ok.setText("确定");
                                return;
                            }
                            try {
                                long a = Long.parseLong(s.toString());
                                long ka = (a & 0xff0000) >> 16;
                                long kb = a & 0xffff;
                                tv.setText(String.format("卡片内容：%010d %03d,%05d", a, ka, kb));
                                divider1.setVisibility(View.VISIBLE);
                                tv.setVisibility(View.VISIBLE);
                                long[] ans = parseData(a);
                                tv2.setText("写入数据：" + String.format("%08X %08X", ans[2], ans[3]));
                                divider2.setVisibility(View.VISIBLE);
                                tv2.setVisibility(View.VISIBLE);
                                tab2ok.setBackground(getResources().getDrawable(R.drawable.corner_d));
                                tab2ok.setText("点击写入数据（请确认卡放在读卡器上）");
                            } catch (Exception e) {
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
                    Button tab2ok = (Button) findViewById(R.id.tab2ok);
                    tab2ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            do_write();
                        }
                    });
                }
            }


            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
    public long[] parseData(long id) {
        long[] ret = new long[4];
        ret[0] = ret[3] = 0;
        ret[1] = 0x5f800100L;
        int i, j, ecc;
        ret[2] = 0x7fc00;
        long t, tt;
        ecc = 0;

        for (i = 7; i > 5; i--) {
            ecc = 0;
            t = (id & 0xf << (i * 4)) >> (i * 4);
            for (j = 3; j >= 0; j--) {
                tt = (t & 1 << j) >> j;
                ecc += tt;
                ret[2] <<= 1;
                ret[2] += tt;
            }
            ret[2] <<= 1;
            ret[2] += ecc & 1;
        }
        ecc = 0;
        t = (id & 0xf << (5 * 4)) >> (5 * 4);
        for (j = 3; j > 0; j--) {
            tt = (t & 1 << j) >> j;
            ecc += tt;
            ret[2] <<= 1;
            ret[2] += tt;
        }
        //ret2 ok
        tt = t & 1;
        ecc += tt;
        ret[3] = tt;
        ret[3] <<= 1;
        ret[3] += ecc & 1;

        for (i = 4; i >= 0; i--) {
            ecc = 0;
            t = (id & 0xf << (i * 4)) >> (i * 4);
            for (j = 3; j >= 0; j--) {
                tt = (t & 1 << j) >> j;
                ecc += tt;
                ret[3] <<= 1;
                ret[3] += tt;
            }
            ret[3] <<= 1;
            ret[3] += ecc & 1;
        }
        for (i = 3; i >= 0; i--) {
            ecc = 0;
            for (j = 7; j >= 0; j--) {
                ecc += (id & 1 << (j * 4 + i)) >> (j * 4 + i);
            }
            ret[3] <<= 1;
            ret[3] += ecc & 1;
        }
        ret[3] <<= 1;
        int it, itt;
        System.out.println(ret[2] + " " + ret[3]);
        //parse ok,reverse
        int ch[] = {0, 8, 4, 12, 2, 10, 6, 14, 1, 9, 5, 13, 3, 12, 7, 15};
        String str = String.format("%08X", ret[2]);
        ret[2] = 0;
        for (i = 0; i < 7; i += 2) {
            if (Character.isDigit(str.charAt(i))) it = str.charAt(i) - '0';
            else it = str.charAt(i) - 'A' + 10;
            if (Character.isDigit(str.charAt(i + 1))) itt = str.charAt(i + 1) - '0';
            else itt = str.charAt(i + 1) - 'A' + 10;

            ret[2] <<= 4;
            ret[2] += ch[itt];
            ret[2] <<= 4;
            ret[2] += ch[it];
        }
        str = String.format("%08X", ret[3]);
        ret[3] = 0;
        for (i = 0; i < 7; i += 2) {
            if (Character.isDigit(str.charAt(i))) it = str.charAt(i) - '0';
            else it = str.charAt(i) - 'A' + 10;
            if (Character.isDigit(str.charAt(i + 1))) itt = str.charAt(i + 1) - '0';
            else itt = str.charAt(i + 1) - 'A' + 10;
            System.out.println("t:" + t + " tt:" + tt + "cht:" + ch[it] + "chtt:" + ch[itt]);
            ret[3] <<= 4;
            ret[3] += ch[itt];
            ret[3] <<= 4;
            ret[3] += ch[it];
        }
        System.out.println("final:" + ret[2] + " " + ret[3]);
        return ret;
    }
    private long[] ansid;
    private void do_write(){
        EditText tab2id = (EditText) findViewById(R.id.tab2id);
        String s = tab2id.getText().toString();
        if (s.length() == 0) return;
        try {
            long a = Long.parseLong(s);
            ansid = parseData(a);
            CardView cv = (CardView) findViewById(R.id.tab2card2);
            cv.setVisibility(View.VISIBLE);
            final TextView echo = (TextView) findViewById(R.id.tab2echo);
            echo.setText("");
            /*for(int i=0;i<8;i++){
                byte[] to_send;
                if(i%2==1){
                    to_send=gen_read((byte)(3+i/2));
                }else{
                    to_send=gen_write((byte)(3+i/2),ansid[i/2]);
                }
                for(int j=0;j<to_send.length;j++){
                    echo.append(String.format("%02X ",to_send[j]));
                }
                echo.append("\n");
                echo.append(String.format("%08X\n",ansid[i/2]));
            }
            */
            UsbDriver.driver = new CH34xUARTDriver(
                    (UsbManager) getSystemService(Context.USB_SERVICE), this,
                    "cn.wch.wchusbdriver.USB_PERMISSION");
            if(!UsbDriver.driver.UsbFeatureSupported()){
                echo.setText(notSupportHost(ansid));
                return;
            }
            if(isOpen) {
                UsbDriver.driver.CloseDevice();
                isOpen = false;
            }
            if(!UsbDriver.driver.ResumeUsbList()){
                echo.setText("打开设备失败");
                UsbDriver.driver.CloseDevice();
                return;
            }else{
                if(!UsbDriver.driver.UartInit()) {//对串口设备进行初始化操作
                    echo.setText("设备初始化失败");
                    UsbDriver.driver.CloseDevice();
                    return;
                }
                echo.setText("打开设备成功！");
                isOpen = true;
                if (UsbDriver.driver.SetConfig(9600, (byte)8, (byte)1, (byte)0, (byte)0)) {
                    echo.setText("设置串口成功！\n"+echo.getText());
                } else {
                    echo.setText("设置串口失败！\n"+echo.getText());
                    return;
                }
                new readThread().start();//开启读线程读取串口接收的数据
                handler = new Handler() {
                    public void handleMessage(Message msg) {
                        do_write_proc((String) msg.obj);
                    }
                };
            }
            stage=0;error=0;
            do_write_proc("");

        } catch (Exception e) {
            TextView echo = (TextView) findViewById(R.id.tab2echo);
            echo.setText(e.getMessage());
        }
    }
    private int stage=0,error=0;
    private void do_write_proc(String s){
        final TextView echo = (TextView) findViewById(R.id.tab2echo);
        if(s.length()!=0&&stage!=0){
            if(stage%2==1){
                int[] ans=parse_back(s);
                if(ans[0]!=0){
                    stage--;
                    error++;
                    echo.setText(String.format("Page%d 写入错误。\n",stage/2+3)+echo.getText());
                }else {
                    echo.setText(String.format("Page%d 写入成功，正在校验...\n", stage / 2+3) + echo.getText());
                }
            }else{
                int[] ans=parse_back(s);
                long data=ansid[stage/2-1];
                if(ans[0]!=0||
                        (data>>24&0xff)!=ans[1]||
                        (data>>16&0xff)!=ans[2]||
                        (data>>8&0xff)!=ans[3]||
                        (data&0xff)!=ans[4]){
                    stage-=2;
                    error++;
                    echo.setText(String.format("Page%d 数据出错，重新写入...\n",stage/2+2)+echo.getText());
                }else {
                    echo.setText(String.format("Page%d 校验成功\n", stage / 2+2) + echo.getText());
                }
            }
        }
        if(stage>=8){
            echo.setText("复制成功！\n" + echo.getText());
            return;
        }
        if(error>=10){
            echo.setText("复制失败\n" + echo.getText());
            return;
        }
        byte[] to_send;
        if(stage%2==1){
            to_send=gen_read((byte)(3+stage/2));
        }else{
            to_send=gen_write((byte)(3+stage/2),ansid[stage/2]);
        }
        int retval = UsbDriver.driver.WriteData(to_send, to_send.length);
        if (retval < 0){
            echo.setText("读卡器访问失败！\n"+echo.getText());
            return;
        }
        stage++;
    }
    private boolean isOpen=false;
    private Handler handler;
    private class readThread extends Thread {
        public void run() {
            byte[] buffer = new byte[64];
            while (true) {
                Message msg = Message.obtain();
                if (!isOpen) {
                    break;
                }
                int length = UsbDriver.driver.ReadData(buffer, 64);
                if (length > 0) {
                    String recv = toHexString(buffer, length);
                    msg.obj = recv;
                    handler.sendMessage(msg);
                }
            }
        }
    }

    /**
     * 将byte[]数组转化为String类型
     * @param arg
     *            需要转换的byte[]数组
     * @param length
     *            需要转换的数组长度
     * @return 转换后的String队形
     */
    private String toHexString(byte[] arg, int length) {
        String result = new String();
        if (arg != null) {
            for (int i = 0; i < length; i++) {
                result = result
                        + (Integer.toHexString(
                        arg[i] < 0 ? arg[i] + 256 : arg[i]).length() == 1 ? "0"
                        + Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])
                        : Integer.toHexString(arg[i] < 0 ? arg[i] + 256
                        : arg[i])) + " ";
            }
            return result;
        }
        return "";
    }
    /**
     * 将String转化为byte[]数组
     * @param arg
     *            需要转换的String对象
     * @return 转换后的byte[]数组
     */
    private byte[] toByteArray(String arg) {
        if (arg != null) {
			/* 1.先去除String中的' '，然后将String转换为char数组 */
            char[] NewArray = new char[1000];
            char[] array = arg.toCharArray();
            int length = 0;
            for (int i = 0; i < array.length; i++) {
                if (array[i] != ' ') {
                    NewArray[length] = array[i];
                    length++;
                }
            }
			/* 将char数组中的值转成一个实际的十进制数组 */
            int EvenLength = (length % 2 == 0) ? length : length + 1;
            if (EvenLength != 0) {
                int[] data = new int[EvenLength];
                data[EvenLength - 1] = 0;
                for (int i = 0; i < length; i++) {
                    if (NewArray[i] >= '0' && NewArray[i] <= '9') {
                        data[i] = NewArray[i] - '0';
                    } else if (NewArray[i] >= 'a' && NewArray[i] <= 'f') {
                        data[i] = NewArray[i] - 'a' + 10;
                    } else if (NewArray[i] >= 'A' && NewArray[i] <= 'F') {
                        data[i] = NewArray[i] - 'A' + 10;
                    }
                }
				/* 将 每个char的值每两个组成一个16进制数据 */
                byte[] byteArray = new byte[EvenLength / 2];
                for (int i = 0; i < EvenLength / 2; i++) {
                    byteArray[i] = (byte) (data[i * 2] * 16 + data[i * 2 + 1]);
                }
                return byteArray;
            }
        }
        return new byte[] {};
    }

    private String notSupportHost(long[] data){
        return "您的手机不支持USB HOST，或者禁用了该功能。\n"+
                "如果无法使用USB HOST，请使用电脑上的写卡软件，并在Page3~Page6写入以下数据：\n"+
                "Page3:  00000000\n"+
                "Page4:  5F800100\n"+
                String.format("Page5:  %08X\n",data[2])+
                String.format("Page6:  %08X\n",data[3]);
    }
    private String notSupportHost(){
        return "您的手机不支持USB HOST，或者禁用了该功能。\n";
    }
    private byte gen_bcc(byte[]a){
        byte ans=a[1];
        int i;
        for(i=2;i<=2+a[2];i++)
            ans^=a[i];
        return ans;
    }
    private byte[] gen_read(byte page){
        byte[] ret={(byte)0xaa,(byte)0x0a,0x2,(byte)0x85,0x0,0x0,(byte)0xbb};
        ret[4]=(byte)page;
        ret[5]=gen_bcc(ret);
        return ret;
    }
    private byte[] gen_write(byte page, long data){
        byte[] ret={(byte)0xaa,0xa,0x6,(byte)0x84,0x0,0x0,0x0,0x0,0x0,0x0,(byte)0xbb};
        ret[4]=page;
        ret[5]=(byte)(data>>24&0xff);
        ret[6]=(byte)(data>>16&0xff);
        ret[7]=(byte)(data>>8&0xff);
        ret[8]=(byte)(data&0xff);
        ret[9]=gen_bcc(ret);
        return ret;
    }
    private int[] parse_back(String back){
        byte[] msg=toByteArray(back);
        int[]ret=new int[5];
        if(msg[0]!=(byte)0xaa||msg[1]!=0xa){ret[0]=0x85;return ret;}
        if(msg[3]!=0){
            if(msg[4]>=0)
                ret[0]=msg[4];
            else
                ret[0]=256+msg[4];
            return ret;
        }
        if(msg[3+msg[2]]!=gen_bcc(msg)){ret[0]=0x1;return ret;}
        ret[0]=0;
        int i;
        for(i=0;i<msg[2]-1;i++){
            if(msg[4+i]>=0)
                ret[1+i]=msg[4+i];
            else
                ret[1+i]=256+msg[4+i];
        }
        return ret;
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
    public void readall(View v){
        final TextView echo=(TextView)findViewById(R.id.tab1echo);
        UsbDriver.driver = new CH34xUARTDriver(
                (UsbManager) getSystemService(Context.USB_SERVICE), this,
                "cn.wch.wchusbdriver.USB_PERMISSION");
        if(!UsbDriver.driver.UsbFeatureSupported()){
            echo.setText(notSupportHost());
            return;
        }
        if(isOpen) {
            UsbDriver.driver.CloseDevice();
            isOpen = false;
        }
        if(!UsbDriver.driver.ResumeUsbList()){
            echo.setText("打开设备失败");
            UsbDriver.driver.CloseDevice();
            return;
        }else{
            if(!UsbDriver.driver.UartInit()) {//对串口设备进行初始化操作
                echo.setText("设备初始化失败");
                UsbDriver.driver.CloseDevice();
                return;
            }
            echo.setText("打开设备成功！");
            isOpen = true;
            if (UsbDriver.driver.SetConfig(9600, (byte)8, (byte)1, (byte)0, (byte)0)) {
                echo.setText("设置串口成功！\n"+echo.getText());
            } else {
                echo.setText("设置串口失败！\n"+echo.getText());
                return;
            }
            new readThread().start();//开启读线程读取串口接收的数据
            handler = new Handler() {
                public void handleMessage(Message msg) {
                    read_all_do((String) msg.obj);
                }
            };
        }
        read_all_proc=0;read_all_err=0;
        read_all_do("");
    }
    private byte read_all_proc=0,read_all_err=0;
    private void read_all_do(String s){
        if(read_all_proc==2)read_all_proc++;
        if(read_all_proc>15)return;
        if(s.length()!=0){
            int[] ret=parse_back(s);
            if(ret[0]==0){
                String st=String.format("%02X%02X%02X%02X",ret[1],ret[2],ret[3],ret[4]);
                int resid=getResources().getIdentifier("tab1t"+read_all_proc+"1","id",getBaseContext().getPackageName());
                TextView tv=(TextView)findViewById(resid);
                tv.setText(st);
                read_all_proc++;
                byte[] to_send=gen_read(read_all_proc);
                UsbDriver.driver.WriteData(to_send, to_send.length);
            }else{
                byte[] to_send=gen_read(read_all_proc);
                UsbDriver.driver.WriteData(to_send, to_send.length);
                read_all_err++;
                if(read_all_err>10)return;
            }
        }else{
            read_all_proc=0;
            byte[] to_send=gen_read(read_all_proc);
            UsbDriver.driver.WriteData(to_send, to_send.length);
        }
    }
    public void writeall(View v){
        final TextView echo=(TextView)findViewById(R.id.tab1echo);
        echo.append("还不支持该功能\n");
    }
}
