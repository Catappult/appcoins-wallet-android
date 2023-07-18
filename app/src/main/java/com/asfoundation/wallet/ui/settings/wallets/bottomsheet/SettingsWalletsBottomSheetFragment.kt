package com.asfoundation.wallet.ui.settings.wallets.bottomsheet

//Commented because moved into a new class, temporarily
//@AndroidEntryPoint
/*
class SettingsWalletsBottomSheetFragment : BasePageViewFragment(), SettingsWalletsBottomSheetView {

  @Inject
  lateinit var currencyFormatter: CurrencyFormatUtils

  @Inject
  lateinit var presenter: SettingsWalletsBottomSheetPresenter

  private var uiEventListener: PublishSubject<String>? = null

  //private val binding by viewBinding(SettingsWalletBottomSheetLayoutBinding::bind)

  companion object {

    const val WALLET_MODEL_KEY = "wallet_model"

    @JvmStatic
    fun newInstance(walletsModel: WalletsModel) =
      SettingsWalletsBottomSheetFragment().apply {
        arguments = Bundle().apply {
          putSerializable(WALLET_MODEL_KEY, walletsModel)
        }
      }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    uiEventListener = PublishSubject.create()
  }
/*
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = SettingsWalletBottomSheetLayoutBinding.inflate(inflater).root


 */
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    //presenter.present()
  }

  override fun setupUi(walletsBalance: List<WalletInfoSimple>) {
    TODO("Not yet implemented")
  }
  /*
    override fun setupUi(walletInfoSimple: List<WalletInfoSimple>) {
      with(binding.bottomSheetWalletsCards) {
        addBottomItemDecoration(resources.getDimension(R.dimen.wallets_card_margin))
        isNestedScrollingEnabled = false
        layoutManager = LinearLayoutManager(context).apply {
          orientation = RecyclerView.VERTICAL
        }
        adapter = WalletsAdapter(walletInfoSimple, uiEventListener!!, currencyFormatter)
      }
      provideParentFragment()?.showBottomSheet()
    }

   */

  override fun walletCardClicked() = uiEventListener!!
  override fun updateAdapterOnClick() {
    TODO("Not yet implemented")
  }

  private fun provideParentFragment(): SettingsWalletsView? =
    if (parentFragment !is SettingsWalletsView) {
      null
    } else {
      parentFragment as SettingsWalletsView
    }

  override fun onDestroyView() {
    super.onDestroyView()
    presenter.stop()
  }
}

 */
